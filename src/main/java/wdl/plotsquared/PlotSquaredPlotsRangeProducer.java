package wdl.plotsquared;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotFilter;
import com.intellectualcrafters.plot.object.PlotId;
import com.plotsquared.bukkit.events.PlayerClaimPlotEvent;
import com.plotsquared.bukkit.events.PlotDeleteEvent;
import com.plotsquared.bukkit.events.PlotMergeEvent;
import com.plotsquared.bukkit.events.PlotUnlinkEvent;

public class PlotSquaredPlotsRangeProducer implements IRangeProducer, Listener {
	private final IRangeGroup rangeGroup;
	private final OwnershipType ownershipType;
	
	public PlotSquaredPlotsRangeProducer(IRangeGroup group, OwnershipType ownershipType) {
		this.rangeGroup = group;
		this.ownershipType = ownershipType;
	}

	@Override
	public List<ProtectionRange> getInitialRanges(final Player player) {
		Set<Plot> plots = PS.get().getPlots(new PlotFilter() {
			@Override
			public boolean allowsArea(PlotArea area) {
				return area.worldname.equals(player.getWorld().getName());
			}
			
			@Override
			public boolean allowsPlot(Plot plot) {
				return ownershipType.isValidPlotForPlayer(player, plot);
			}
		});
		List<ProtectionRange> returned = new ArrayList<>();
		for (Plot plot : plots) {
			returned.add(convertPlot(plot));
		}
		return returned;
	}
	
	/**
	 * Converts a Plot into a ProtectionRange.
	 */
	private ProtectionRange convertPlot(Plot plot) {
		ChunkLoc bottom = plot.getBottomAbs().getChunkLoc();
		ChunkLoc top = plot.getTopAbs().getChunkLoc();
		String tag = getPlotTag(plot);
		return new ProtectionRange(tag, bottom.x, bottom.z, top.x, top.z);
	}
	
	/**
	 * Gets a chunk override tag that can be used for a plot.
	 * 
	 * This is different from the default alias for a plot (which is the area
	 * followed by the ID), to improve hashability client side (more prominent variance
	 * from changes in x/z).  Also, it won't change when the alias is manually changed.
	 */
	private String getPlotTag(Plot plot) {
		return plot.getId() + ";" + plot.getArea();
	}

	@Override
	public IRangeGroup getRangeGroup() {
		return rangeGroup;
	}

	@Override
	public void dispose() {
		// Stop listening to events
		HandlerList.unregisterAll(this);
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlotClaimed(PlayerClaimPlotEvent event) {
		Plot plot = event.getPlot();
		ProtectionRange range = convertPlot(plot);
		World world = Bukkit.getWorld(plot.getArea().worldname);
		Set<UUID> players = ownershipType.getApplicablePlayers(plot);
		
		world.getPlayers().stream()
				.filter(p -> rangeGroup.isWDLPlayer(p))
				.filter(p -> players.contains(p.getUniqueId()))
				.forEach(p -> rangeGroup.addRanges(p, range));
	}
	
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlotRemoved(PlotDeleteEvent event) {
		Plot plot = event.getPlot();
		String tag = getPlotTag(plot);
		World world = Bukkit.getWorld(plot.getArea().worldname);
		Set<UUID> players = ownershipType.getApplicablePlayers(plot);
		
		world.getPlayers().stream()
				.filter(p -> rangeGroup.isWDLPlayer(p))
				.filter(p -> players.contains(p.getUniqueId()))
				.forEach(p -> rangeGroup.removeRangesByTags(p, tag));
	}
	
	/**
	 * Resend all merged plots to account for changed border areas.
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlotMerge(PlotMergeEvent event) {
		ArrayList<PlotId> plotIDs = event.getPlots();
		PlotArea area = event.getPlot().getArea();
		List<Plot> plots = plotIDs.stream()
				.map(id -> PS.get().getPlot(area, id))
				.collect(Collectors.toList());
		List<ProtectionRange> ranges = plots.stream()
				.map(plot -> convertPlot(plot))
				.collect(Collectors.toList());
		List<String> tags = plots.stream()
				.map(plot -> getPlotTag(plot))
				.collect(Collectors.toList());
		
		World world = event.getWorld();
		Set<UUID> players = ownershipType.getApplicablePlayers(event.getPlot());
		
		world.getPlayers().stream()
				.filter(p -> rangeGroup.isWDLPlayer(p))
				.filter(p -> players.contains(p.getUniqueId()))
				.forEach(p -> {
					rangeGroup.removeRangesByTags(p, tags);
					rangeGroup.addRanges(p, ranges);
				});
	}
	
	/**
	 * Resend all unmerged plots to account for changed border areas.
	 */
	@EventHandler(priority=EventPriority.MONITOR, ignoreCancelled=true)
	public void onPlotUnmerge(PlotUnlinkEvent event) {
		ArrayList<PlotId> plotIDs = event.getPlots();
		PlotArea area = event.getArea();
		List<Plot> plots = plotIDs.stream()
				.map(id -> PS.get().getPlot(area, id))
				.collect(Collectors.toList());
		List<ProtectionRange> ranges = plots.stream()
				.map(plot -> convertPlot(plot))
				.collect(Collectors.toList());
		List<String> tags = plots.stream()
				.map(plot -> getPlotTag(plot))
				.collect(Collectors.toList());
		
		World world = event.getWorld();
		// We use the first plot in the list for simplicity - they'll all have the
		// same permissions at this point
		// This does assume that there is at least one plot in the list, but for
		// this type of event that seems reasonable
		Set<UUID> players = ownershipType.getApplicablePlayers(plots.get(0));
		
		world.getPlayers().stream()
				.filter(p -> rangeGroup.isWDLPlayer(p))
				.filter(p -> players.contains(p.getUniqueId()))
				.forEach(p -> {
					rangeGroup.removeRangesByTags(p, tags);
					rangeGroup.addRanges(p, ranges);
				});
	}
}
