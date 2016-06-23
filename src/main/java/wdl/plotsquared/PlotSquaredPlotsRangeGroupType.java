package wdl.plotsquared;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import wdl.range.IRangeGroup;
import wdl.range.IRangeGroupType;

public class PlotSquaredPlotsRangeGroupType implements IRangeGroupType<PlotSquaredPlotsRangeProducer> {
	@Override
	public boolean isValidConfig(ConfigurationSection config,
			List<String> warnings, List<String> errors) {
		return true;
	}

	@Override
	public PlotSquaredPlotsRangeProducer createRangeProducer(IRangeGroup group,
			ConfigurationSection config) {
		return new PlotSquaredPlotsRangeProducer(group);
	}

	@Override
	public void dispose() {
		
	}

}
