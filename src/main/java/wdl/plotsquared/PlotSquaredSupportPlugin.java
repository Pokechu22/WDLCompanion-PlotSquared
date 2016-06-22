package wdl.plotsquared;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;
import org.mcstats.Metrics.Graph;
import org.mcstats.Metrics.Plotter;

import wdl.RangeGroupTypeRegistrationEvent;
import wdl.range.IRangeProducer;

import com.intellectualcrafters.plot.PS;

/**
 * WDLCompanion support plugin for 
 * <a href="https://www.spigotmc.org/resources/plotsquared.1177/">PlotSquared</a>
 */
public class PlotSquaredSupportPlugin extends JavaPlugin implements Listener {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		
		try {
			class StringPlotter extends Plotter {
				public StringPlotter(String str) {
					super(str);
				}
				
				@Override
				public int getValue() {
					return 1;
				}
			}
			
			Metrics metrics = new Metrics(this);
			
			Graph plotSquaredVersionGraph = metrics.createGraph("plotSquaredVersion");
			String plotSquaredVersion = getProvidingPlugin(PS.class)
					.getDescription().getFullName();
			plotSquaredVersionGraph.addPlotter(new StringPlotter(plotSquaredVersion));
			
			Graph wdlcVersionGraph = metrics.createGraph("wdlcompanionVersion");
			String wdlcVersion = getProvidingPlugin(IRangeProducer.class)
					.getDescription().getFullName();
			wdlcVersionGraph.addPlotter(new StringPlotter(wdlcVersion));
			
			metrics.start();
		} catch (Exception e) {
			getLogger().warning("Failed to start PluginMetrics :(");
		}
	}
	
	@EventHandler
	public void registerRangeGroupTypes(RangeGroupTypeRegistrationEvent e) {
		
	}
}
