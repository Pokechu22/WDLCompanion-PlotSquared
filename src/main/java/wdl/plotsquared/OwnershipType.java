package wdl.plotsquared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.intellectualcrafters.plot.object.Plot;

/**
 * Types of ownership / membership of a plot.
 * 
 * Trusted seems to be able to use the plot even when isOnline() is false, while
 * member seems not to. Thus, trusted is treated as above member.
 */
public enum OwnershipType {
	LEADER("leader") {
		@Override
		public boolean isValidPlotForPlayer(Player player, Plot plot) {
			return plot.isOwner(player.getUniqueId());
		}
	},
	TRUSTED("trusted") {
		@Override
		public boolean isValidPlotForPlayer(Player player, Plot plot) {
			return plot.isOwner(player.getUniqueId())
					|| plot.getTrusted().contains(player.getUniqueId());
		}
	},
	MEMBER("member") {
		@Override
		public boolean isValidPlotForPlayer(Player player, Plot plot) {
			return plot.isOwner(player.getUniqueId())
					|| plot.getTrusted().contains(player.getUniqueId())
					|| plot.getMembers().contains(player.getUniqueId());
		}
	},
	ANY("any", "all") {
		@Override
		public boolean isValidPlotForPlayer(Player player, Plot plot) {
			return plot.isAdded(player.getUniqueId());
		}
	};
	
	/**
	 * All aliases used by this ownership type.
	 */
	public final List<String> aliases;
	/**
	 * All valid names for ownership types.
	 */
	public static final List<String> NAMES;
	/**
	 * A map of aliases to instances.  Note: Keys should be toUpperCased()
	 */
	private static final Map<String, OwnershipType> BY_ALIAS;
	
	/**
	 * Constructor.
	 * 
	 * @param aliases Possible names that can be found in the configuration.
	 */
	private OwnershipType(String... aliases) {
		this.aliases = ImmutableList.copyOf(aliases);
	}
	
	/**
	 * Does the given player have the required type of ownership in the given
	 * region?
	 */
	public abstract boolean isValidPlotForPlayer(Player player, Plot plot);
	
	/**
	 * Gets the OwnershipType with the given name or alias.
	 * 
	 * @param name the alias
	 * @return the type, or null if it can't be found.
	 */
	public static OwnershipType match(String name) {
		return BY_ALIAS.get(name.toUpperCase());
	}
	
	static {
		List<String> names = new ArrayList<String>();
		Map<String, OwnershipType> byAlias = new HashMap<>();
		for (OwnershipType type : values()) {
			for (String alias : type.aliases) {
				names.add(alias);
				byAlias.put(alias.toUpperCase(), type);
			}
		}
		
		NAMES = ImmutableList.copyOf(names);
		BY_ALIAS = ImmutableMap.copyOf(byAlias);
	}
}
