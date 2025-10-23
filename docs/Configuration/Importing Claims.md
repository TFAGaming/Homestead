# Importing Claims

Are you migrating from a different anti-grief plugin to Homestead? Follow the steps:

1. Do not uninstall the plugin or delete the plugin's data.
2. When Homestead is ready, use the command `/hsadmin migratedata [plugin]` to import data from that plugin.
3. Wait for a few minutes until the command no longer sends messages in the console.

!!! warning "Importing From Other Plugins"

    Some anti-grief plugins may have different claiming systems, and Homestead does its best to migrate data. If something isn't working as intended, please report the issue to the developers.

## Supported Plugins

| Plugins | Supported? |
| ------- | :--------: |
| GriefPrevention | Yes |
| ClaimChunk | Yes |
| LandLord4 | Yes |
| Lands | No |
| Residence | No |
