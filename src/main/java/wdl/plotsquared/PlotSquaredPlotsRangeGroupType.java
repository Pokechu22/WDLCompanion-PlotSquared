package wdl.plotsquared;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class PlotSquaredPlotsRangeGroupType implements IRangeGroupType<PlotSquaredPlotsRangeProducer> {
	private final Plugin plugin;
	
	public PlotSquaredPlotsRangeGroupType(Plugin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		if (!config.isSet("ownershipType")) {
			warnings.add("'ownershipType' is not set!  The default, 'any', will be used.");
		} else if (!config.isString("ownershipType")) {
			errors.add("'ownershipType' is not a String!");
			return false;
		} else if (OwnershipType.match(config.getString("ownershipType")) == null) {
			errors.add("'ownershipType' is not valid!  Should be one of " + OwnershipType.NAMES + ", got '" + config.getString("ownershipType") + "'!");
			return false;
		}
		return true;
	}

	@Override
	public PlotSquaredPlotsRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		OwnershipType type = OwnershipType.match(config.getString("ownershipType", "any"));
		PlotSquaredPlotsRangeProducer producer = new PlotSquaredPlotsRangeProducer(group,
				type);
		plugin.getServer().getPluginManager().registerEvents(producer, plugin);
		
		return producer;
	}

	@Override
	public void dispose() {
		
	}

}
