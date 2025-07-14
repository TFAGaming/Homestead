package tfagaming.projects.minecraft.homestead.integrations;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.database.Database;
import tfagaming.projects.minecraft.homestead.integrations.bstats.Metrics;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;

public class bStats {
    public bStats(Homestead plugin) {
        try {
			Metrics metrics = new Metrics(plugin, 25286);

			// Regions
			metrics.addCustomChart(new Metrics.SingleLineChart("regions", new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					return RegionsManager.getAll().size();
				}
			}));

			// Sub-Areas
			metrics.addCustomChart(new Metrics.SingleLineChart("subareas", new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					int subAreas = 0;

                    for (Region region : RegionsManager.getAll()) {
                        subAreas += region.getSubAreas().size();
                    }

                    return subAreas;
				}
			}));

			// Trusted Players
			metrics.addCustomChart(new Metrics.SingleLineChart("trusted_players", new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					int players = 0;

					for (Region region : RegionsManager.getAll()) {
						players += region.getMembers().size();
					}

					return players;
				}
			}));

			// Chunks
			metrics.addCustomChart(new Metrics.SingleLineChart("chunks", new Callable<Integer>() {
				@Override
				public Integer call() throws Exception {
					int chunks = 0;

					for (Region region : RegionsManager.getAll()) {
						chunks += region.getChunks().size();
					}

					return chunks;
				}
			}));

			// Database
			metrics.addCustomChart(new Metrics.AdvancedPie("database_provider", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() throws Exception {
					Map<String, Integer> map = new HashMap<>();

					switch (Database.parseProviderFromString(Homestead.database.getSelectedProvider())) {
						case PostgreSQL:
							map.put("PostgreSQL", 1);
							break;
						case MySQL:
							map.put("MySQL", 1);
							break;
						case SQLite:
							map.put("SQLite", 1);
							break;
						case YAML:
							map.put("YAML", 1);
							break;
						default:
							break;
					}

					return map;
				}
			}));

			// Dynamic Maps
			metrics.addCustomChart(new Metrics.AdvancedPie("dynamic_maps", new Callable<Map<String, Integer>>() {
				@Override
				public Map<String, Integer> call() throws Exception {
					Map<String, Integer> map = new HashMap<>();

					map.put("Dynmap", DynamicMaps.dynmap == null ? 0 : 1);
					map.put("Squaremap", DynamicMaps.squaremap == null ? 0 : 1);
					map.put("Pl3xMap", DynamicMaps.pl3xmap == null ? 0 : 1);
					map.put("BlueMap", DynamicMaps.bluemap == null ? 0 : 1);

					return map;
				}
			}));
            
		} catch (NoClassDefFoundError | IllegalStateException e) {
			Logger.error("Unable to communicate with bStats servers.");
		}
    }
}
