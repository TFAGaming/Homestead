package tfagaming.projects.minecraft.homestead.database.providers;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.War;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

public class SQLite {
	private static final String JDBC_URL = "jdbc:sqlite:";

	private Connection connection;

	public SQLite(String dbFile) {
		try {
			Class.forName("org.sqlite.JDBC");

			this.connection = DriverManager.getConnection(JDBC_URL + dbFile);

			Logger.info("New SQLite database connection established.");

			createTablesIfNotExists();
		} catch (ClassNotFoundException e) {
			Logger.error("SQLite JDBC Driver not found.");
			e.printStackTrace();

			Homestead.getInstance().endInstance();
		} catch (SQLException e) {
			Logger.error("Unable to establish a connection for SQLite.");
			e.printStackTrace();

			Homestead.getInstance().endInstance();
		}
	}

	public SQLite(String dbFile, boolean handleError) {
		try {
			Class.forName("org.sqlite.JDBC");

			this.connection = DriverManager.getConnection(JDBC_URL + dbFile);

			Logger.info("New SQLite database connection established.");

			createTablesIfNotExists();
		} catch (ClassNotFoundException e) {
			Logger.error("SQLite JDBC Driver not found.");
			e.printStackTrace();

			if (!handleError) {
				Homestead.getInstance().endInstance();
			}
		} catch (SQLException e) {
			Logger.error("Unable to establish a connection for SQLite.");
			e.printStackTrace();

			if (!handleError) {
				Homestead.getInstance().endInstance();
			}
		}
	}

	public void createTablesIfNotExists() {
		String sql1 = "CREATE TABLE IF NOT EXISTS regions (" +
				"id TEXT PRIMARY KEY, " +
				"displayName TEXT NOT NULL, " +
				"name TEXT NOT NULL, " +
				"description TEXT NOT NULL, " +
				"ownerId TEXT NOT NULL, " +
				"location TEXT, " +
				"createdAt INTEGER NOT NULL, " +
				"playerFlags INTEGER NOT NULL, " +
				"worldFlags INTEGER NOT NULL, " +
				"bank REAL NOT NULL, " +
				"mapColor INTEGER NOT NULL, " +
				"chunks TEXT NOT NULL, " +
				"members TEXT NOT NULL, " +
				"rates TEXT NOT NULL, " +
				"invitedPlayers TEXT NOT NULL, " +
				"bannedPlayers TEXT NOT NULL, " +
				"subAreas TEXT NOT NULL, " +
				"logs TEXT NOT NULL, " +
				"rent TEXT, " +
				"upkeepAt INTEGER NOT NULL, " +
				"taxesAmount REAL NOT NULL, " +
				"weather INTEGER NOT NULL, " +
				"time INTEGER NOT NULL, " +
				"welcomeSign TEXT," +
				"icon TEXT" +
				")";

		String sql2 = "CREATE TABLE IF NOT EXISTS wars (" +
				"id TEXT PRIMARY KEY, " +
				"displayName TEXT NOT NULL, " +
				"name TEXT NOT NULL, " +
				"description TEXT NOT NULL, " +
				"regions TEXT NOT NULL, " +
				"prize REAL NOT NULL, " +
				"startedAt INTEGER NOT NULL" +
				")";

		try (Statement stmt = connection.createStatement()) {
			stmt.executeUpdate(sql1);
			stmt.executeUpdate(sql2);
		} catch (SQLException e) {
			Logger.error("An unexpected error occurred for the provider: " + Homestead.database.getSelectedProvider());
			e.printStackTrace();
		}
	}

	public void importRegions() {
		String sql = "SELECT * FROM regions";

		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			Homestead.regionsCache.clear();

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				String displayName = rs.getString("displayName");
				String name = rs.getString("name");
				String description = rs.getString("description");
				OfflinePlayer owner = Homestead.getInstance()
						.getOfflinePlayerSync(UUID.fromString(rs.getString("ownerId")));
				SerializableLocation location = SerializableLocation
						.fromString(rs.getString("location"));
				long createdAt = rs.getLong("createdAt");
				long playerFlags = rs.getLong("playerFlags");
				long worldFlags = rs.getLong("worldFlags");
				double bank = rs.getDouble("bank");
				int mapColor = rs.getInt("mapColor");
				List<SerializableChunk> chunks = rs.getString("chunks").length() > 0
						? Arrays.asList(rs.getString("chunks").split("§")).stream()
								.map(SerializableChunk::fromString)
								.collect(Collectors.toList())
						: new ArrayList<>();
				List<SerializableMember> members = rs.getString("members").length() > 0
						? Arrays.asList(rs.getString("members").split("§")).stream()
								.map(SerializableMember::fromString)
								.collect(Collectors.toList())
						: new ArrayList<>();
				List<SerializableRate> rates = rs.getString("rates").length() > 0
						? Arrays.asList(rs.getString("rates").split("§")).stream()
								.map(SerializableRate::fromString)
								.collect(Collectors.toList())
						: new ArrayList<>();
				List<OfflinePlayer> invitedPlayers = rs.getString("invitedPlayers").length() > 0
						? Arrays.asList(rs.getString("invitedPlayers").split("§")).stream()
								.map((uuidString) -> Homestead.getInstance()
										.getOfflinePlayerSync(UUID.fromString(
												uuidString)))
								.collect(Collectors.toList())
						: new ArrayList<>();
				List<SerializableBannedPlayer> bannedPlayers = rs.getString("bannedPlayers")
						.length() > 0
								? Arrays.asList(rs.getString("bannedPlayers")
										.split("§"))
										.stream()
										.map(SerializableBannedPlayer::fromString)
										.collect(Collectors.toList())
								: new ArrayList<>();
				List<SerializableLog> logs = rs.getString("logs").length() > 0
						? Arrays.asList(rs.getString("logs").split("µ")).stream()
								.map(SerializableLog::fromString)
								.collect(Collectors.toList())
						: new ArrayList<>();
				List<SerializableSubArea> subAreas = rs.getString("subAreas").length() > 0
						? Arrays.asList(rs.getString("subAreas").split("§")).stream()
								.map(SerializableSubArea::fromString)
								.collect(Collectors.toList())
						: new ArrayList<>();
				SerializableRent rent = SerializableRent.fromString(rs.getString("rent"));
				long upkeepAt = rs.getLong("upkeepAt");
				double taxesAmount = rs.getDouble("taxesAmount");
				int weather = rs.getInt("weather");
				int time = rs.getInt("time");
				SerializableLocation welcomeSign = rs.getString("welcomeSign") == null ? null
						: SerializableLocation.fromString(rs.getString("welcomeSign"));
				String icon = rs.getString("icon") == null ? null : rs.getString("icon");

				if (owner == null) {
					continue;
				}

				Region region = new Region(name, owner);
				region.id = id;
				region.displayName = displayName;
				region.description = description;
				region.location = location;
				region.createdAt = createdAt;
				region.playerFlags = playerFlags;
				region.worldFlags = worldFlags;
				region.bank = bank;
				region.mapColor = mapColor;
				region.setChunks(chunks);
				region.setMembers(members);
				region.setRates(rates);
				region.setInvitedPlayers(ListUtils.removeNullElements(invitedPlayers));
				region.setBannedPlayers(bannedPlayers);
				region.setLogs(logs);
				region.setSubAreas(subAreas);
				region.rent = rent;
				region.upkeepAt = upkeepAt;
				region.taxesAmount = taxesAmount;
				region.weather = weather;
				region.time = time;
				region.welcomeSign = welcomeSign;
				region.icon = icon;

				Homestead.regionsCache.putOrUpdate(region);
			}
		} catch (SQLException e) {
			Logger.error("An unexpected error occurred for the provider: " + Homestead.database.getSelectedProvider());
			e.printStackTrace();
		}

		Logger.info("Imported " + Homestead.regionsCache.size() + " regions.");
	}

	public void importWars() {
		String sql = "SELECT * FROM wars";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			Homestead.warsCache.clear();

			while (rs.next()) {
				UUID id = UUID.fromString(rs.getString("id"));
				String displayName = rs.getString("displayName");
				String name = rs.getString("name");
				String description = rs.getString("description");
				List<UUID> regions = rs.getString("regions").length() > 0
						? Arrays.asList(rs.getString("regions").split("§")).stream()
						.map(UUID::fromString).collect(Collectors.toList())
						: new ArrayList<>();
				double prize = rs.getDouble("prize");
				long startedAt = rs.getLong("startedAt");

				War war = new War(name, regions);
				war.id = id;
				war.displayName = displayName;
				war.description = description;
				war.prize = prize;
				war.startedAt = startedAt;

				Homestead.warsCache.putOrUpdate(war);
			}
		} catch (SQLException e) {
			Logger.error("An unexpected error occurred for the provider: " + Homestead.database.getSelectedProvider());
			e.printStackTrace();
		}

		Logger.info("Imported " + Homestead.warsCache.size() + " wars.");
	}

	public void exportRegions() {
		Set<UUID> dbRegionIds = new HashSet<>();
		String selectSql = "SELECT id FROM regions";

		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbRegionIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Logger.error("An unexpected error occurred for the provider: " + Homestead.database.getSelectedProvider());
			e.printStackTrace();

			return;
		}

		String upsertSql = "INSERT OR REPLACE INTO regions (" +
				"id, displayName, name, description, ownerId, location, createdAt, " +
				"playerFlags, worldFlags, bank, mapColor, chunks, members, rates, " +
				"invitedPlayers, bannedPlayers, subAreas, logs, rent, upkeepAt, taxesAmount, weather, " +
				"time, welcomeSign, icon" +
				") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String deleteSql = "DELETE FROM regions WHERE id = ?";

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
				PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheRegionIds = new HashSet<>();

			for (Region region : Homestead.regionsCache.getAll()) {
				UUID regionId = region.id;
				cacheRegionIds.add(regionId);

				String chunksStr = String.join("§",
						region.chunks.stream().map(SerializableChunk::toString)
								.collect(Collectors.toList()));
				String membersStr = String.join("§",
						region.members.stream().map(SerializableMember::toString)
								.collect(Collectors.toList()));
				String ratesStr = String.join("§",
						region.rates.stream().map(SerializableRate::toString)
								.collect(Collectors.toList()));
				String invitedStr = String.join("§",
						region.getInvitedPlayers().stream().map(OfflinePlayer::getUniqueId)
								.map(UUID::toString).collect(Collectors.toList()));
				String bannedStr = String.join("§",
						region.bannedPlayers.stream().map(SerializableBannedPlayer::toString)
								.collect(Collectors.toList()));
				String logsStr = String.join("µ",
						region.logs.stream().map(SerializableLog::toString)
								.collect(Collectors.toList()));
				String subAreasStr = String.join("§",
						region.subAreas.stream().map(SerializableSubArea::toString)
								.collect(Collectors.toList()));

				upsertStmt.setString(1, regionId.toString());
				upsertStmt.setString(2, region.displayName);
				upsertStmt.setString(3, region.name);
				upsertStmt.setString(4, region.description);
				upsertStmt.setString(5, region.getOwnerId().toString());
				upsertStmt.setString(6, region.location != null ? region.location.toString() : null);
				upsertStmt.setLong(7, region.createdAt);
				upsertStmt.setLong(8, region.playerFlags);
				upsertStmt.setLong(9, region.worldFlags);
				upsertStmt.setDouble(10, region.bank);
				upsertStmt.setInt(11, region.mapColor);
				upsertStmt.setString(12, chunksStr);
				upsertStmt.setString(13, membersStr);
				upsertStmt.setString(14, ratesStr);
				upsertStmt.setString(15, invitedStr);
				upsertStmt.setString(16, bannedStr);
				upsertStmt.setString(17, subAreasStr);
				upsertStmt.setString(18, logsStr);
				upsertStmt.setString(19, region.rent != null ? region.rent.toString() : null);
				upsertStmt.setLong(20, region.upkeepAt);
				upsertStmt.setDouble(21, region.taxesAmount);
				upsertStmt.setInt(22, region.weather);
				upsertStmt.setInt(23, region.time);
				upsertStmt.setString(24,
						region.welcomeSign != null ? region.welcomeSign.toString() : null);
				upsertStmt.setString(25, region.icon != null ? region.icon.toString() : null);

				upsertStmt.addBatch();
			}

			upsertStmt.executeBatch();

			dbRegionIds.removeAll(cacheRegionIds);
			for (UUID deletedId : dbRegionIds) {
				deleteStmt.setString(1, deletedId.toString());
				deleteStmt.addBatch();
			}

			deleteStmt.executeBatch();

			if (Homestead.config.isDebugEnabled()) {
				Logger.info("Exported " + cacheRegionIds.size() + " regions and deleted " + dbRegionIds.size()
						+ " regions.");
			}
		} catch (SQLException e) {
			Logger.error("An unexpected error occurred for the provider: " + Homestead.database.getSelectedProvider());
			e.printStackTrace();
		}
	}

	public void exportWars() {
		Set<UUID> dbWarIds = new HashSet<>();
		String selectSql = "SELECT id FROM wars";

		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery(selectSql)) {
			while (rs.next()) {
				dbWarIds.add(UUID.fromString(rs.getString("id")));
			}
		} catch (SQLException e) {
			Logger.error("An unexpected error occurred for the provider: " + Homestead.database.getSelectedProvider());
			e.printStackTrace();

			return;
		}

		String upsertSql = "INSERT OR REPLACE INTO  wars (" +
				"id, displayName, name, description, regions, prize, startedAt" +
				") VALUES (?, ?, ?, ?, ?, ?, ?)";

		String deleteSql = "DELETE FROM wars WHERE id = ?";

		try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
			 PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
			Set<UUID> cacheWarIds = new HashSet<>();

			for (War war : Homestead.warsCache.getAll()) {
				UUID warId = war.id;
				cacheWarIds.add(warId);

				String regionsStr = String.join("§",
						war.regions.stream().map(UUID::toString).collect(Collectors.toList()));

				upsertStmt.setString(1, warId.toString());
				upsertStmt.setString(2, war.displayName);
				upsertStmt.setString(3, war.name);
				upsertStmt.setString(4, war.description);
				upsertStmt.setString(5, regionsStr);
				upsertStmt.setDouble(6, war.prize);
				upsertStmt.setLong(7, war.startedAt);

				upsertStmt.addBatch();
			}

			upsertStmt.executeBatch();

			dbWarIds.removeAll(cacheWarIds);
			for (UUID deletedId : dbWarIds) {
				deleteStmt.setString(1, deletedId.toString());
				deleteStmt.addBatch();
			}

			deleteStmt.executeBatch();

			if (Homestead.config.isDebugEnabled()) {
				Logger.info("Exported " + cacheWarIds.size() + " wars and deleted " + dbWarIds.size()
						+ " wars.");
			}
		} catch (SQLException e) {
			Logger.error("An unexpected error occurred for the provider: " + Homestead.database.getSelectedProvider());
			e.printStackTrace();
		}
	}

	public void closeConnection() {
		try {
			if (connection != null && !connection.isClosed()) {
				connection.close();
				Logger.warning("Connection for SQLite has been closed.");
			}
		} catch (SQLException e) {
			Logger.error("An unexpected error occurred for the provider: " + Homestead.database.getSelectedProvider());
			e.printStackTrace();
		}
	}

	public long getLatency() {
		long before = System.currentTimeMillis();

		String sql = "SELECT * FROM regions";

		try (Statement stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
			}
		} catch (SQLException e) {
			return -1L;
		}

		long after = System.currentTimeMillis();

		return after - before;
	}
}
