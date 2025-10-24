# Database

A database is a way to store data for all regions on a server. Homestead provides a built-in caching system to improve the performance and speed for protecting and managing all regions.

## Providers

| Provider        | Implemented? | Notes |
| :--------------- |:------------:| :----- |
| PostgreSQL       |     Yes      | For big servers (~1,000 players daily)    
| MariaDB          |     Yes      | For big servers (~1,000 players daily) 
| MySQL            |     Yes      | For big servers (~1,000 players daily)
| SQLite (Default) |     Yes      | For small servers (~100 players daily)
| YAML             |     Yes      | For small and friends-only servers (~50 players)

## Setup

!!! warning "Important notice for PostgreSQL and MySQL"

    For **PostgreSQL**, you must create a new database with the name "homestead_data".<br>
    For **MySQL**, you must create a new schema with the name "homestead_data".

By default, when you install Homestead, the selected provider is **SQLite**.

If you have never let other players use Homestead, or you used it for testing before public joining, and you want to change the database provider, change the current provider name to the provider you want to use. These are the valid provider names:

- PostgreSQL: `postgresql`
- MariaDB: `mariadb`
- MySQL: `mysql`
- SQLite: `sqlite`
- YAML: `yaml`

```yaml
database:
  provider: "sqlite"

  # PostgreSQL configuration:
  postgresql:
    username: "USERNAME"
    password: "PASSWORD"
    host: "localhost"
    port: 3306
    
  # MariaDB configuration:
  mariadb:
    username: "USERNAME"
    password: "PASSWORD"
    host: "localhost"
    port: 3306

  # MySQL configuration:
  mysql:
    username: "USERNAME"
    password: "PASSWORD"
    host: "localhost"
    port: 3306

  # SQLite configuration:
  # NOTE: The data file will be saved in the server's main directory.
  sqlite: "homestead_data.db"
```

## Migrating to another provider


1. Prepare your new provider's details (username, password...) in the **config.yml** file.
2. Save the changes and reload the configuration using the command: `/hsadmin reload`
3. Migrate the current data to the new provider using the command: `/hsadmin migratedata [provider]`
4. Stop your server.
5. Change the provider to the provider you migrated to earlier in **config.yml**.
6. Start the server. You're good to go!
