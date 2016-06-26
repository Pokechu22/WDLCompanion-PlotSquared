package wdl.plotsquared;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

import wdl.range.IRangeGroup;
import wdl.range.IRangeProducer;
import wdl.range.ProtectionRange;

import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.object.ChunkLoc;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotArea;
import com.intellectualcrafters.plot.object.PlotFilter;

public class PlotSquaredPlotsRangeProducer implements IRangeProducer {
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
			ChunkLoc bottom = plot.getBottomAbs().getChunkLoc();
			ChunkLoc top = plot.getTopAbs().getChunkLoc();
			String tag = getPlotTag(plot);
			returned.add(new ProtectionRange(tag, bottom.x, bottom.z, top.x, top.z));
		}
		return returned;
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
		
	}

}
