# Integrations and Plugins

Homestead uses a lot of different APIs to communicate with other dependencies and implement as many features as possible for a Minecraft server.

## WorldGuard
**WorldGuard** is another grief prevention plugin mostly used to protect spawns, PVP arenas... etc.

To prevent players from claiming chunks inside a WorldGuard-protected area, enable this feature:

```yaml
worldguard:
  protect-existing-regions: true
```

## WorldEdit
Homestead uses a unique feature that only exists in **WorldEdit**, which is regenerating chunks.

!!! warning "Deprecated Feature"

    This feature is deprecated by WorldEdit developers, and it's not recommended to keep it enabled when your server has very active players. Regenerating chunks requires a significant amount of memory, especially when a player or operator deletes a region with more than 4 claimed chunks.

To enable the feature, you must install WorldEdit and have this configuration enabled in **config.yml**:

```yaml
worldedit:
  regenerate-chunks: false
```

## PlaceholderAPI
**PlaceholderAPI** is a plugin that allows server owners to display information from various plugins in a uniform format.

Available placeholders:

- `%homestead_region_bank%`: Displays the region's bank balance.
- `%homestead_region_name%`: Shows the name of the region.
- `%homestead_region_current%`: Shows the name of the region that the player is currently inside.
- `%homestead_region_claimed_chunks%`: Indicates the total number of claimed chunks in the region.
- `%homestead_region_max_chunks%`: Displays the maximum number of chunks a player can claim.
- `%homestead_region_trusted_members%`: Indicates the total number of members in the region.
- `%homestead_region_max_members%`: Displays the maximum number of chunks a player can claim.
- `%homestead_upkeep_amount%`: Displays the required amount to pay for taxes.
- `%homestead_upkeep_at%`: Shows when the region's owners must pay their taxes.

## Web Map Rendering Plugins

**dynmap**, **Pl3xMap**, **Squaremap**, and **BlueMap** are Google Maps-like plugins that show all claimed chunks in marked boxes.

Install one of these plugins, and restart the server. You should be able to see all claimed chunks via these links:

- **BlueMap**: http://localhost:8100/
- **Squaremap**: http://localhost:8080/
- **Pl3xMap**: http://localhost:8080/
- **dynmap**: http://localhost:8123/

```yaml
dynamic-maps:
  # Enable this feature? "true" to enable, "false" to disable.
  enabled: true

  # The interval to update the regions on the web, use seconds.
  update-interval: 60 # 60 seconds (1 minute)

  # The icons for regions.
  # NOTE: ".png"s are the only supported image format.
  icons:
    # Enable this feature? "true" to enable, "false" to disable.
    # NOTE: If you enable icons, players can set custom icons for their regions.
    enabled: true

    # The default icon.
    default: https://imgur.com/TUQzlCK.png

    # The size for icons, must be a valid integer.
    size: 20 # 20x20

    # The list of icons that players can use. You can add as many icons as you want!
    list:
      ...

  # Customization; HTML syntax is supported!
  chunks:
    # For chunks that are owned by a server operator.
    operator-color: 0xFF0000 # RGB; Red, Green, Blue: 255, 0, 0
    operator-description: ...

    # For chunks that are owned by ordinary players (non operators).
    color: 0x00FF00 # RGB; Red, Green, Blue: 0, 255, 0
    description: ...

    # For every chunk.
    # These features currently only works for the BLueMap Plugin.
    transparency-fill: 40 # Value 0 - 255 (For now only made for BlueMapAPI)
    transparency-outline: 125 # # Value 0 - 255 (For now only made for BlueMapAPI)
```

