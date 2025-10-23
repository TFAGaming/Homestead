# Ranks and Permissions
## Ranks and the limits
Each player has a group; by default, it's `default`. They may upgrade to another rank, for example, `vip` group, so they need special upgrades for their regions, like 6+ more claims, 4+ members... etc.

There are two options for limits:
- **groups**: This will make Homestead use LuckPerms (or any other permissions plugin) to fetch a player's group and then get the group's limit.
- **static**: This will make Homestead not use LuckPerms or any other permissions plugin, but all players will have the same limits (not depending on groups). Note that operators will have different limits.

```yml
limits:
  # Two valid options:
  # - 'static': Static limits; No permission plugins required.
  # - 'groups': Group limits; Permissions and groups plugin required (LuckPerms...)
  method: 'static'
```

To change the limits, edit the following settings:

> **Caution**: If a group is not found, any limit from that group will be 0 by default. Please make sure that all groups exist in the **config.yml** file.

```yml
limits:
  ...

  # The limits for player groups.
  groups:
    # Group: default
    default:
      regions: 2
      chunks-per-region: 10
      members-per-region: 4
      subareas-per-region: 2
      max-subarea-volume: 400
      commands-cooldown: 2

    # Group: vip
    vip:
      regions: 4
      chunks-per-region: 20
      members-per-region: 8
      subareas-per-region: 5
      max-subarea-volume: 800
      commands-cooldown: 2

    # Group: admin
    admin:
      regions: 10
      chunks-per-region: 100
      members-per-region: 16
      subareas-per-region: 10
      max-subarea-volume: 1200
      commands-cooldown: 0

  # The limits for operators and non-op players.
  static:
    # Limits for normal players (non operators):
    non-op:
      regions: 2
      chunks-per-region: 10
      members-per-region: 4
      subareas-per-region: 2
      max-subarea-volume: 400
      commands-cooldown: 2

    # Limits for server operators:
    op:
      regions: 10
      chunks-per-region: 100
      members-per-region: 16
      subareas-per-region: 10
      max-subarea-volume: 1200
      commands-cooldown: 0
```

## Permissions

In the following guide, you will know how to set permissions for a group using the LuckPerms plugin.

1. Use **/lp editor** to generate a link for the web editor of LuckPerms. Once the link is generated, click on it and open it in a web browser.
2. On your left screen, go to "Groups", and then select "default". The "default" group is the group that is automatically given to newly joined players.
3. At the bottom of the page, there is a combobox with a placeholder "Enter permissions", click on it. Select the permissions you want to allow players to use their commands, for example, if you select `homestead.commands.region.create`, this will allow players with the "default" group to use the command **/region create <args>**.
4. Once you select all permissions, click on "+ Add". But before that, if you want to disallow the permissions for the selected group, click on "true" to change it to "false", and then "+ Add".
5. You're good to go! You may need to leave the server and rejoin to update the commands for the client-side.

### Operators / Administrators:
- homestead.commands.homesteadadmin.importdata
- homestead.commands.homesteadadmin.migratedata
- homestead.commands.homesteadadmin.plugin
- homestead.commands.homesteadadmin.reload
- homestead.commands.homesteadadmin.updates
- homestead.operator

The `homestead.operator` permission is unique; It allows you to:
1. Bypass any disabled flag for any region.
2. Set any region as the targeted region; You can manage any region.
3. Claim chunks adjacent to other regions.
4. Ignored from: Delayed teleports, Taxes, Renting...

### Region Management Permissions:
Specific permissions for players to manage their regions.

- homestead.region.\*: This will give the group with all the permissions below.
- homestead.region.create
- homestead.region.delete
- homestead.region.players.trust
- homestead.region.players.untrust
- homestead.region.players.ban
- homestead.region.players.unban
- homestead.region.flags.global
- homestead.region.flags.world
- homestead.region.flags.members
- homestead.region.subareas.create
- homestead.region.subareas.delete
- homestead.region.subareas.rename
- homestead.region.subareas.flags
- homestead.region.bank
- homestead.region.dynamicmaps.icon
- homestead.region.dynamicmaps.color

### Command Permissions

- homestead.commands.region.\*: This will give the group with all the permissions below.
- homestead.commands.region.accept
- homestead.commands.region.banlist
- homestead.commands.region.ban
- homestead.commands.region.claimlist
- homestead.commands.region.create
- homestead.commands.region.delete
- homestead.commands.region.deny
- homestead.commands.region.deposit
- homestead.commands.region.flags
- homestead.commands.region.help
- homestead.commands.region.home
- homestead.commands.region.logs
- homestead.commands.region.members
- homestead.commands.region.menu
- homestead.commands.region.player
- homestead.commands.region.borders
- homestead.commands.region.info
- homestead.commands.region.rename
- homestead.commands.region.set
- homestead.commands.region.subareas
- homestead.commands.region.trust
- homestead.commands.region.unban
- homestead.commands.region.untrust
- homestead.commands.region.visit
- homestead.commands.region.withdraw
