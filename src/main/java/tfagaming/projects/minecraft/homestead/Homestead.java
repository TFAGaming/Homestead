package tfagaming.projects.minecraft.homestead;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.lucko.commodore.Commodore;
import me.lucko.commodore.CommodoreProvider;
import tfagaming.projects.minecraft.homestead.commands.CommandBuilder;
import tfagaming.projects.minecraft.homestead.commands.MojangBrigadier;
import tfagaming.projects.minecraft.homestead.commands.commands.*;
import tfagaming.projects.minecraft.homestead.config.*;
import tfagaming.projects.minecraft.homestead.database.*;
import tfagaming.projects.minecraft.homestead.events.*;
import tfagaming.projects.minecraft.homestead.integrations.*;
import tfagaming.projects.minecraft.homestead.integrations.maps.RegionIconTools;
import tfagaming.projects.minecraft.homestead.listeners.*;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.tools.https.UpdateChecker;
import tfagaming.projects.minecraft.homestead.tools.minecraft.plugins.IntegrationsUtils;
import tfagaming.projects.minecraft.homestead.tools.validator.YAMLValidator;

public class Homestead extends JavaPlugin {
	private final static String version = "4.0.4";
	private static Homestead instance;
	private static long startedAt;

	public static Database database;
	public static RegionsCache cache;

	public static ConfigLoader config;
	public static LanguageLoader language;
	public static MenusConfigLoader menusConfig;

	public static Vault vault;

	public void onEnable() {
		Homestead.instance = this;
		Homestead.startedAt = System.currentTimeMillis();

		if (!getDataFolder().exists()) {
			getDataFolder().mkdirs();
		}

		File regionsFolder = new File(getDataFolder(), "regions");
		if (!regionsFolder.exists()) {
			regionsFolder.mkdir();
		}

		new Logger();

		saveDefaultConfig();

		config = new ConfigLoader(this);

		language = new LanguageLoader(this, config.get("language"));

		menusConfig = new MenusConfigLoader(this);

		Set<String> skipKeys = new HashSet<>();

		YAMLValidator configValidator = new YAMLValidator("config.yml", new File(getDataFolder(), "config.yml"),
				skipKeys);

		if (!configValidator.validate()) {
			boolean fixed = configValidator.fix();

			if (fixed) {
				config = new ConfigLoader(this);
			} else {
				endInstance();
				return;
			}
		}

		YAMLValidator languageValidator = new YAMLValidator("en-US.yml",
				language.getLanguageFile(config.get("language")));

		if (!languageValidator.validate()) {
			boolean fixed = languageValidator.fix();

			if (fixed) {
				language = new LanguageLoader(this, config.get("language"));
			} else {
				endInstance();
				return;
			}
		}

		YAMLValidator menusConfigValidator = new YAMLValidator("menus.yml", new File(getDataFolder(), "menus.yml"),
				skipKeys);

		if (!menusConfigValidator.validate()) {
			boolean fixed = menusConfigValidator.fix();

			if (fixed) {
				menusConfig = new MenusConfigLoader(this);
			} else {
				endInstance();
				return;
			}
		}

		Homestead.cache = new RegionsCache(config.get("cache-interval"));

		Database.Provider provider = Database.parseProviderFromString(config.get("database.provider"));

		if (provider == null) {
			Logger.error("Invalid database provider, please use: PostgreSQL, MySQL, SQLite, YAML");
			endInstance();
		}

		Homestead.database = new Database(provider);

		File claimsFolder = new File(getDataFolder(), "claims");
		if (claimsFolder.exists()) {
			Logger.warning("Detected \"claims\" folder, importing old regions data...");

			int __a = OldDataLoader.loadRegions();

			Logger.warning("Imported " + __a + " regions, deleting the \"claims\" folder...");

			if (!OldDataLoader.deleteDirectory(claimsFolder)) {
				for (int i = 0; i < 100; i++) {
					Logger.error("Unable to delete the \"claims\" folder, please delete it manually.");
				}

				endInstance();

				return;
			} else {
				Logger.warning("The migration was successfully done, welcome to version " + getVersion() + "!");

				database.exportRegions();
			}
		} else {
			database.importRegions();
		}

		if (!IntegrationsUtils.isVaultInstalled()) {
			Logger.error("Unable to start the plugin; \"Vault\" is required. Shutting down plugin instance...");
			endInstance();
			return;
		} else {
			Logger.info("\"Vault\" found, loading service providers...");
		}

		Homestead.vault = new Vault(this);

		if (!Homestead.vault.setupEconomy()) {
			Logger.warning("No Economy service provider found.");
			Logger.warning("Any feature requiring an Economy service will be skipped.");
		} else {
			Logger.info("Loaded service provider: Economy [" + Homestead.vault.getEconomy().getName() + "]");
		}

		if (!Homestead.vault.setupPermissions()) {
			Logger.error("No Permissions service provider found.");
			Logger.error("Permissions are required for Homestead to run. Shutting down plugin instance...");
			endInstance();
			return;
		} else {
			Logger.info("Loaded service provider: Permissions [" + Homestead.vault.getPermissions().getName() + "]");
		}

		registerCommands();
		registerEvents();
		registerBrigadier();

		new bStats(this);

		if (Homestead.config.isDebugEnabled()) {
			Logger.warning("Debug mode is enabled in config.yml; logs.txt may be flooded with warnings.");
		}

		Logger.info("Ready, took " + String.valueOf(System.currentTimeMillis() - startedAt) + " ms to load.");

		runAsyncTask(() -> {
			Logger.warning("Downloading required web map render icons... This may take a while!");
			RegionIconTools.downloadAllIcons();
			Logger.info("Successfully downloaded all icons.");
		});

		runAsyncTimerTask(() -> {
			runAsyncTask(() -> {
				new DynamicMaps(this);
			});
		}, Homestead.config.get("dynamic-maps.update-interval"));

		if (Homestead.vault.isEconomyReady() && (boolean) Homestead.config.get("taxes.enabled")) {
			runAsyncTimerTask(() -> {
				new MemberTaxes(this);
			}, 10);
		}

		if (Homestead.vault.isEconomyReady() && (boolean) Homestead.config.get("upkeep.enabled")) {
			runAsyncTimerTask(() -> {
				new RegionUpkeep(this);
			}, 10);
		}

		if (Homestead.vault.isEconomyReady() && (boolean) Homestead.config.get("renting.enabled")) {
			runAsyncTimerTask(() -> {
				new RegionRent(this);
			}, 10);
		}

		runAsyncTimerTask(() -> {
			runAsyncTask(() -> {
				new UpdateChecker(this);
			});
		}, 86400);

		registerExternalPlugins();

		initOptionalBlueMapIntegration();
	}

	private void registerCommands() {
		CommandBuilder.register(new RegionCommand());
		CommandBuilder.register(new ClaimCommand());
		CommandBuilder.register(new UnclaimCommand());
		CommandBuilder.register(new HomesteadAdminCommand());
		CommandBuilder.register(new ForceUnclaimCommand());
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(new PlayerRegionEnterAndExitListener(), this);
		getServer().getPluginManager().registerEvents(new RegionProtectionListener(), this);
		getServer().getPluginManager().registerEvents(new SelectionToolListener(), this);
		getServer().getPluginManager().registerEvents(new CommandsCooldownListener(), this);
		getServer().getPluginManager().registerEvents(new CustomSignsLisntener(), this);
		getServer().getPluginManager().registerEvents(new PlayerAutoClaimListener(), this);
	}

	private void registerBrigadier() {
		try {
			if (CommodoreProvider.isSupported()) {
				Commodore commodore = CommodoreProvider.getCommodore(this);
				new MojangBrigadier(this, commodore);
			} else {
				Logger.warning("Mojang Brigadier is not supported on this server software.");
			}
		} catch (NoClassDefFoundError e) {
			Logger.warning("Commodore/Brigadier classes not present. Skipping Brigadier command registration.");
		}
	}

	/**
	 * Optionally initialize BlueMap integration when BlueMap is installed and the API is present.
	 * Uses a defensive classpath check to avoid NoClassDefFoundError when BlueMap is absent.
	 */
	private void initOptionalBlueMapIntegration() {
		try {
			boolean enabled = getServer().getPluginManager().isPluginEnabled("BlueMap");
			if (!enabled) {
				Logger.info("[Maps] BlueMap is not installed — skipping BlueMap integration.");
				return;
			}

			Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");

			de.bluecolored.bluemap.api.BlueMapAPI.onEnable(api -> {
				try {
					Logger.info("[Maps] BlueMap detected — enabling BlueMap integration.");
					new tfagaming.projects.minecraft.homestead.integrations.maps.BlueMapAPI(this, api);
				} catch (Throwable t) {
					Logger.error("[Maps] Failed to initialize BlueMap integration: " + t.getClass().getName() + ": " + t.getMessage());
				}
			});

			Logger.info("[Maps] BlueMap API hook registered.");

		} catch (ClassNotFoundException e) {
			Logger.info("[Maps] BlueMap API is not on the classpath — skipping BlueMap integration.");
		} catch (Throwable t) {
			Logger.error("[Maps] Unexpected error while setting up BlueMap integration: " + t.getClass().getName() + ": " + t.getMessage());
		}
	}

	/**
	 * Run a repeating task asynchronously with interval in seconds.
	 *
	 * @param callable The task to run.
	 * @param interval The interval, in seconds.
	 */
	public void runAsyncTimerTask(Runnable callable, int interval) {
		long intervalTicks = interval * 20L;

		Bukkit.getScheduler().runTaskTimerAsynchronously(this, callable, 0L, intervalTicks);
	}

	/**
	 * Run a repeating task asynchronously with interval in seconds, with a delay in seconds.
	 *
	 * @param callable The task to run.
	 * @param interval The interval, in seconds.
	 */
	public void runAsyncTimerTask(Runnable callable, int delay, int interval) {
		long delayTicks = delay * 20L;
		long intervalTicks = interval * 20L;

		Bukkit.getScheduler().runTaskTimerAsynchronously(this, callable, delayTicks, intervalTicks);
	}

	/**
	 * Run a task asynchronously after a delay in seconds.
	 *
	 * @param callable The task to run.
	 * @param delay    The delay, in seconds.
	 */
	public void runAsyncTaskLater(Runnable callable, int delay) {
		long delayTicks = delay * 20L;

		Bukkit.getScheduler().runTaskLaterAsynchronously(this, callable, delayTicks);
	}

	/**
	 * Run a task asynchronously.
	 *
	 * @param callable The task to run.
	 */
	public void runAsyncTask(Runnable callable) {
		Bukkit.getScheduler().runTaskAsynchronously(this, callable);
	}

	/**
	 * Run a task synchronously.
	 *
	 * @param callable The task to run.
	 */
	public void runSyncTask(Runnable callable) {
		Bukkit.getScheduler().runTask(this, callable);
	}

	/**
	 * Get a list of offline players.
	 */
	public List<OfflinePlayer> getOfflinePlayersSync() {
		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

		return Arrays.asList(offlinePlayers);
	}

	/**
	 * Get an offline player with player unique IDs, using safe method.
	 *
	 * @param playerId The player ID.
	 */
	public OfflinePlayer getOfflinePlayerSync(UUID playerId) {
		Player onlinePlayer = Bukkit.getPlayer(playerId);

		if (onlinePlayer != null) {
			return onlinePlayer;
		}

		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

		for (OfflinePlayer player : offlinePlayers) {
			if (player.getName() != null && player.hasPlayedBefore() && player.getUniqueId().equals(playerId)) {
				return player;
			}
		}

		return null;
	}

	/**
	 * Get an offline player with player name, using safe method.
	 *
	 * @param playerName The player name.
	 */
	public OfflinePlayer getOfflinePlayerSync(String playerName) {
		Player onlinePlayer = Bukkit.getPlayer(playerName);

		if (onlinePlayer != null) {
			return onlinePlayer;
		}

		OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();

		for (OfflinePlayer player : offlinePlayers) {
			if (player.getName() != null && player.hasPlayedBefore() && player.getName().equals(playerName)) {
				return player;
			}
		}

		return null;
	}

	public void onDisable() {
		if (database != null) {
			Logger.info("Closing database connection...");
			database.closeConnection();
		}
	}

	public static String getVersion() {
		return version;
	}

	public static Homestead getInstance() {
		return instance;
	}

	public void registerExternalPlugins() {
		if (isPlaceholderAPIInstalled()) {
			boolean placeholderRegistered = new PlaceholderAPI(this).register();

			if (!placeholderRegistered) {
				Logger.error("Failed to register hooks.");
			}
		}
	}

	public boolean isPlaceholderAPIInstalled() {
		return Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null
				&& Bukkit.getServer().getPluginManager().getPlugin("PlaceholderAPI").isEnabled();
	}

	/**
	 * Kill the plugin's instance.
	 */
	public void endInstance() {
		getServer().getPluginManager().disablePlugin(this);
	}
}
