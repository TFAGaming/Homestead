package tfagaming.projects.minecraft.homestead.database;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.database.providers.MySQL;
import tfagaming.projects.minecraft.homestead.database.providers.PostgreSQL;
import tfagaming.projects.minecraft.homestead.database.providers.SQLite;
import tfagaming.projects.minecraft.homestead.database.providers.YAML;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public class Database {
    private Provider provider;
    private PostgreSQL postgreSQL;
    private MySQL mySQL;
    private SQLite sqLite;
    private YAML yaml;

    public enum Provider {
        PostgreSQL,
        MySQL,
        SQLite,
        YAML
    }

    public Database(Provider provider) {
        this.provider = provider;

        Logger.info("Attempting to connect to database... Provider:", provider.toString());

        switch (provider) {
            case PostgreSQL:
                postgreSQL = new PostgreSQL(Homestead.config.get("database.postgresql.username"),
                        Homestead.config.get("database.postgresql.password"),
                        Homestead.config.get("database.postgresql.host"),
                        Homestead.config.get("database.postgresql.port"));
                break;
            case MySQL:
                mySQL = new MySQL(Homestead.config.get("database.mysql.username"),
                        Homestead.config.get("database.mysql.password"), Homestead.config.get("database.mysql.host"),
                        Homestead.config.get("database.mysql.port"));
                break;
            case SQLite:
                sqLite = new SQLite(Homestead.config.get("database.sqlite"));
                break;
            case YAML:
                yaml = new YAML(Homestead.getInstance().getDataFolder());
                break;
            default:
                break;
        }
    }

    public Database(Provider provider, boolean handleError) {
        this.provider = provider;

        Logger.info("Attempting to connect to database... Provider:", provider.toString());

        switch (provider) {
            case PostgreSQL:
                postgreSQL = new PostgreSQL(Homestead.config.get("database.postgresql.username"),
                        Homestead.config.get("database.postgresql.password"),
                        Homestead.config.get("database.postgresql.host"),
                        Homestead.config.get("database.postgresql.port"), handleError);
                break;
            case MySQL:
                mySQL = new MySQL(Homestead.config.get("database.mysql.username"),
                        Homestead.config.get("database.mysql.password"), Homestead.config.get("database.mysql.host"),
                        Homestead.config.get("database.mysql.port"), handleError);
                break;
            case SQLite:
                sqLite = new SQLite(Homestead.config.get("database.sqlite"), handleError);
                break;
            case YAML:
                yaml = new YAML(Homestead.getInstance().getDataFolder());
                break;
            default:
                break;
        }
    }

    public static Provider parseProviderFromString(String provider) {
        switch (provider.toLowerCase()) {
            case "postgresql":
                return Provider.PostgreSQL;
            case "mysql":
                return Provider.MySQL;
            case "sqlite":
                return Provider.SQLite;
            case "yaml":
                return Provider.YAML;
            default:
                return null;
        }
    }

    public String getSelectedProvider() {
        return provider.toString();
    }

    public void importRegions() {
        switch (provider) {
            case PostgreSQL:
                postgreSQL.importRegions();
                break;
            case MySQL:
                mySQL.importRegions();
                break;
            case SQLite:
                sqLite.importRegions();
                break;
            case YAML:
                yaml.importRegions();
                break;
            default:
                break;
        }
    }

    public void exportRegions() {
        switch (provider) {
            case PostgreSQL:
                postgreSQL.exportRegions();
                break;
            case MySQL:
                mySQL.exportRegions();
                break;
            case SQLite:
                sqLite.exportRegions();
                break;
            case YAML:
                yaml.exportRegions();
                break;
            default:
                break;
        }
    }

    public void closeConnection() {
        switch (provider) {
            case PostgreSQL:
                postgreSQL.closeConnection();
                break;
            case MySQL:
                mySQL.closeConnection();
                break;
            case SQLite:
                sqLite.closeConnection();
                break;
            case YAML:
                yaml.closeConnection();
                break;
            default:
                break;
        }
    }

    public long getLatency() {
        switch (provider) {
            case PostgreSQL:
                return postgreSQL.getLatency();
            case MySQL:
                return mySQL.getLatency();
            case SQLite:
                return sqLite.getLatency();
            case YAML:
                return yaml.getLatency();
            default:
                return -1L;
        }
    }
}
