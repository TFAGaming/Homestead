package tfagaming.projects.minecraft.homestead.database.providers;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.logs.Logger;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.*;
import tfagaming.projects.minecraft.homestead.tools.java.ListUtils;

public class MariaDB {

    private static final String JDBC_URL = "jdbc:mariadb://";
    private Connection connection;

    public MariaDB(String username, String password, String host, int port) {
        this(username, password, host, port, false);
    }

    public MariaDB(String username, String password, String host, int port, boolean handleError) {
        try {
            Class.forName("org.mariadb.jdbc.Driver");

            String url = JDBC_URL + host + ":" + port + "/homestead_data"
                    + "?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
            this.connection = DriverManager.getConnection(url, username, password);

            Logger.info("MariaDB database connection established.");

            createTableIfNotExists();
        } catch (ClassNotFoundException e) {
            Logger.error("MariaDB JDBC Driver not found.");
            e.printStackTrace();

            if (!handleError) Homestead.getInstance().endInstance();
        } catch (SQLException e) {
            Logger.error("Unable to establish connection to MariaDB.");
            e.printStackTrace();

            if (!handleError) Homestead.getInstance().endInstance();
        }
    }

    public void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS regions (
                id              CHAR(36) PRIMARY KEY,
                display_name    TEXT NOT NULL,
                name            TEXT NOT NULL,
                description     TEXT NOT NULL,
                owner_id        CHAR(36) NOT NULL,
                location        TEXT,
                created_at      BIGINT NOT NULL,
                player_flags    BIGINT NOT NULL,
                world_flags     BIGINT NOT NULL,
                bank            DOUBLE PRECISION NOT NULL,
                map_color       INT NOT NULL,
                chunks          TEXT NOT NULL,
                members         TEXT NOT NULL,
                rates           TEXT NOT NULL,
                invited_players TEXT NOT NULL,
                banned_players  TEXT NOT NULL,
                sub_areas       TEXT NOT NULL,
                logs            TEXT NOT NULL,
                rent            TEXT,
                upkeep_at       BIGINT NOT NULL,
                taxes_amount    DOUBLE NOT NULL,
                weather         INT NOT NULL,
                time            INT NOT NULL,
                welcome_sign    TEXT,
                icon            TEXT
            )
        """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            Logger.info("Regions table created/verified in MariaDB.");
        } catch (SQLException e) {
            Logger.error("Unable to create regions table in MariaDB.");
            e.printStackTrace();
        }
    }

    public void importRegions() {
        final String sql = "SELECT * FROM regions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            Homestead.cache.clear();

            while (rs.next()) {
                UUID id = UUID.fromString(rs.getString("id"));
                String displayName = rs.getString("display_name");
                String name = rs.getString("name");
                String description = rs.getString("description");
                OfflinePlayer owner = Homestead.getInstance()
                        .getOfflinePlayerSync(UUID.fromString(rs.getString("owner_id")));

                SerializableLocation location =
                        Optional.ofNullable(rs.getString("location"))
                                .map(SerializableLocation::fromString).orElse(null);

                long createdAt = rs.getLong("created_at");
                long playerFlags = rs.getLong("player_flags");
                long worldFlags = rs.getLong("world_flags");
                double bank = rs.getDouble("bank");
                int mapColor = rs.getInt("map_color");

                List<SerializableChunk> chunks =
                        Arrays.stream(stringArrayFromPgText(rs.getString("chunks")))
                                .map(SerializableChunk::fromString)
                                .collect(Collectors.toList());

                List<SerializableMember> members =
                        Arrays.stream(stringArrayFromPgText(rs.getString("members")))
                                .map(SerializableMember::fromString)
                                .collect(Collectors.toList());

                List<SerializableRate> rates =
                        Arrays.stream(stringArrayFromPgText(rs.getString("rates")))
                                .map(SerializableRate::fromString)
                                .collect(Collectors.toList());

                List<OfflinePlayer> invitedPlayers =
                        uuidArrayFromPgText(rs.getString("invited_players"));

                List<SerializableBannedPlayer> bannedPlayers =
                        Arrays.stream(stringArrayFromPgText(rs.getString("banned_players")))
                                .map(SerializableBannedPlayer::fromString)
                                .collect(Collectors.toList());

                List<SerializableLog> logs =
                        Arrays.stream(stringArrayFromPgText(rs.getString("logs")))
                                .map(SerializableLog::fromString)
                                .collect(Collectors.toList());

                List<SerializableSubArea> subAreas =
                        Arrays.stream(stringArrayFromPgText(rs.getString("sub_areas")))
                                .map(SerializableSubArea::fromString)
                                .collect(Collectors.toList());

                SerializableRent rent =
                        Optional.ofNullable(rs.getString("rent"))
                                .map(SerializableRent::fromString).orElse(null);

                long upkeepAt = rs.getLong("upkeep_at");
                double taxesAmount = rs.getDouble("taxes_amount");
                int weather = rs.getInt("weather");
                int time = rs.getInt("time");

                SerializableLocation welcomeSign =
                        Optional.ofNullable(rs.getString("welcome_sign"))
                                .map(SerializableLocation::fromString).orElse(null);
                String icon = rs.getString("icon");

                if (owner == null) continue;

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

                Homestead.cache.putOrUpdate(region);
            }
        } catch (SQLException e) {
            Logger.error("Unable to import regions from MariaDB.");
            e.printStackTrace();
        }
        Logger.info("Imported " + Homestead.cache.size() + " regions from MariaDB.");
    }

    public void exportRegions() {
        Set<UUID> dbRegionIds = new HashSet<>();
        final String selectSql = "SELECT id FROM regions";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            while (rs.next()) {
                dbRegionIds.add(UUID.fromString(rs.getString("id")));
            }
        } catch (SQLException e) {
            Logger.error("Unable to fetch region IDs from MariaDB.");
            e.printStackTrace();
            return;
        }

        final String upsertSql = """
            INSERT INTO regions (
                id, display_name, name, description, owner_id, location, created_at,
                player_flags, world_flags, bank, map_color, chunks, members, rates,
                invited_players, banned_players, sub_areas, logs, rent, upkeep_at,
                taxes_amount, weather, time, welcome_sign, icon
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                display_name = VALUES(display_name),
                name = VALUES(name),
                description = VALUES(description),
                owner_id = VALUES(owner_id),
                location = VALUES(location),
                created_at = VALUES(created_at),
                player_flags = VALUES(player_flags),
                world_flags = VALUES(world_flags),
                bank = VALUES(bank),
                map_color = VALUES(map_color),
                chunks = VALUES(chunks),
                members = VALUES(members),
                rates = VALUES(rates),
                invited_players = VALUES(invited_players),
                banned_players = VALUES(banned_players),
                sub_areas = VALUES(sub_areas),
                logs = VALUES(logs),
                rent = VALUES(rent),
                upkeep_at = VALUES(upkeep_at),
                taxes_amount = VALUES(taxes_amount),
                weather = VALUES(weather),
                time = VALUES(time),
                welcome_sign = VALUES(welcome_sign),
                icon = VALUES(icon)
        """;

        final String deleteSql = "DELETE FROM regions WHERE id = ?";

        try (PreparedStatement upsertStmt = connection.prepareStatement(upsertSql);
             PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {

            Set<UUID> cacheRegionIds = new HashSet<>();

            for (Region region : Homestead.cache.getAll()) {
                UUID regionId = region.id;
                cacheRegionIds.add(regionId);

                upsertStmt.setString(1, regionId.toString());
                upsertStmt.setString(2, region.displayName);
                upsertStmt.setString(3, region.name);
                upsertStmt.setString(4, region.description);
                upsertStmt.setString(5, region.getOwnerId().toString());
                upsertStmt.setString(6, region.location == null ? null : region.location.toString());
                upsertStmt.setLong(7, region.createdAt);
                upsertStmt.setLong(8, region.playerFlags);
                upsertStmt.setLong(9, region.worldFlags);
                upsertStmt.setDouble(10, region.bank);
                upsertStmt.setInt(11, region.mapColor);

                upsertStmt.setString(12, toPgTextArray(region.chunks.stream()
                        .map(SerializableChunk::toString).toArray(String[]::new)));
                upsertStmt.setString(13, toPgTextArray(region.members.stream()
                        .map(SerializableMember::toString).toArray(String[]::new)));
                upsertStmt.setString(14, toPgTextArray(region.rates.stream()
                        .map(SerializableRate::toString).toArray(String[]::new)));
                upsertStmt.setString(15, toPgTextArray(region.getInvitedPlayers().stream()
                        .map(p -> p.getUniqueId().toString()).toArray(String[]::new)));
                upsertStmt.setString(16, toPgTextArray(region.bannedPlayers.stream()
                        .map(SerializableBannedPlayer::toString).toArray(String[]::new)));
                upsertStmt.setString(17, toPgTextArray(region.logs.stream()
                        .map(SerializableLog::toString).toArray(String[]::new)));
                upsertStmt.setString(18, toPgTextArray(region.subAreas.stream()
                        .map(SerializableSubArea::toString).toArray(String[]::new)));

                upsertStmt.setString(19, region.rent == null ? null : region.rent.toString());
                upsertStmt.setLong(20, region.upkeepAt);
                upsertStmt.setDouble(21, region.taxesAmount);
                upsertStmt.setInt(22, region.weather);
                upsertStmt.setInt(23, region.time);
                upsertStmt.setString(24, region.welcomeSign == null ? null : region.welcomeSign.toString());
                upsertStmt.setString(25, region.icon);

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
                        + " regions from MariaDB.");
            }
        } catch (SQLException e) {
            Logger.error("Unable to export regions to MariaDB.");
            e.printStackTrace();
        }
    }

    private static String toPgTextArray(String[] src) {
        return "{" + String.join(",", src) + "}";
    }

    private static String[] stringArrayFromPgText(String pg) {
        if (pg == null || pg.isEmpty()) return new String[0];
        return pg.replaceAll("^\\{|\\}$", "").split(",", -1);
    }

    private static List<OfflinePlayer> uuidArrayFromPgText(String pg) {
        return Arrays.stream(stringArrayFromPgText(pg))
                .map(id -> Homestead.getInstance().getOfflinePlayerSync(UUID.fromString(id)))
                .collect(Collectors.toList());
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Logger.warning("MariaDB connection has been closed.");
            }
        } catch (SQLException e) {
            Logger.error("Unable to close MariaDB connection.");
            e.printStackTrace();
        }
    }

    public long getLatency() {
        long before = System.currentTimeMillis();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            while (rs.next()) {

            }
        } catch (SQLException e) {
            return -1L;
        }

        return System.currentTimeMillis() - before;
    }
}
