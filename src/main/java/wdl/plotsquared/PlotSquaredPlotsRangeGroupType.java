package wdl.plotsquared;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class PlotSquaredPlotsRangeGroupType implements IRangeGroupType<PlotSquaredPlotsRangeProducer> {
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
		return new PlotSquaredPlotsRangeProducer(group,
				OwnershipType.match(config.getString("ownershipType", "any")));
	}

	@Override
	public void dispose() {
		
	}

}
