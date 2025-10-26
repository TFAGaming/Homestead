# Regions Manager

To get the regions manager, you must call the API variable, like this example:

```java
import tfagaming.projects.minecraft.homestead.managers.*;

RegionsManager manager = api.getRegionsManager();
```

## Functions and Properties

---

### `createRegion(String name, OfflinePlayer player)`
- Builds a new Region object, assigns the owner.  
- If **upkeep** is enabled in config, schedules the first upkeep timestamp.  
- Stores region in cache and fires **RegionCreateEvent** (sync).

---

### `createRegion(String name, OfflinePlayer player, boolean verifyName)`
- Same as above, but when `verifyName == true` it auto-appends a counter until the name is unique.  
- Example: `"Farm"` → `"Farm1"` → `"Farm2"` …

---

### `getAll()`
- Returns **List<Region>** of every loaded region (straight from cache).

---

### `findRegion(UUID id)`  
### `findRegion(String name)`
- Two quick lookup helpers; return **null** when not found.

---

### `deleteRegion(UUID id, OfflinePlayer... player)`
- Removes the region from cache.  
- If **WorldEdit regeneration** is on, every chunk is async-regenerated.  
- Fires **RegionDeleteEvent** (sync).

---

### `addNewLog(UUID id, int messagePath)`  
### `addNewLog(UUID id, int messagePath, Map<String,String> replacements)`
- Adds a **SerializableLog** entry to the region (with optional placeholder replacement).  
- Used for audit trails.

---

### `getAllOwners()`
- Returns **unique** OfflinePlayer list of every region owner (duplicates stripped).

---

### `sortRegionsAlpha()`
- Returns **new List** ordered **A→Z** by region name (case-insensitive).

---

### `getRegionsWithWelcomeSigns()`
- Filters regions that have a **welcome sign** stored.

---

### `getPlayersWithRegionsHasWelcomeSigns()`
- Returns **owners** whose region possesses a welcome sign.

---

### `getRegionsOwnedByPlayer(OfflinePlayer player)`
- All regions whose **owner UUID** matches.

---

### `getRegionsHasPlayerAsMember(OfflinePlayer player)`
- All regions where the player is **explicitly added as member** (not just owner).

---

### `getPublicRegions()`
- Regions whose **player flags** contain both  
  `PASSTHROUGH` **and** `TELEPORT_SPAWN` → treated as "public".

---

### `getRegionsInvitedPlayer(OfflinePlayer player)`
- Regions where the player **has an active invitation** (but has not yet accepted).

---

### `sortRegions(RegionSorting type)`
| Enum value | Sort key (descending) |
|:------------:|-----------------------|
| `BANK` | Region bank balance |
| `CHUNKS_COUNT` | Number of claimed chunks |
| `MEMBERS_COUNT` | Member list size |
| `RATING` | Average player rating |
| `CREATION_DATE` | Oldest → Newest |

---

### `getRank(RegionSorting type, UUID id)`
- Returns **1-based position** in the above sorted list (0 = not ranked).

---

### `getGlobalRank(UUID id)`
- Averages the ranks across **BANK, CHUNKS, MEMBERS, RATING** → simple overall score.

---

### `isNameUsed(String name)`
- Case-insensitive name collision check.

---

### `getAverageRating(Region region)`
- Arithmetic mean of all `SerializableRate` entries; 0.0 if none.

---

### `deleteRegionsWithInvalidPlayerIds()`
- Cleans up **owner-less** or **member-less** UUIDs (players that no longer exist).  
- Returns **count** of removed regions + pruned members.

---

### `setPlayerFlagForAllRegions(long flag, boolean state)`
- Mass-toggle a **player-scope flag** (e.g., PvP, build, interact) on **every** region.  
- Uses `FlagsCalculator.addFlag / removeFlag`.

---

### `setWorldFlagForAllRegions(long flag, boolean state)`
- Same idea, but affects **world-scope flags** (weather, time, explosions, etc.).

---
