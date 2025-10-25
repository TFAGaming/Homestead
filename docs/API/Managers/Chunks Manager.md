# Chunks Manager

To get the chunks manager, you must call the API variable, like this example:

```java
import tfagaming.projects.minecraft.homestead.managers.*;

ChunksManager manager = api.getChunksManager();
```

## Functions and Properties

---

### `claimChunk(UUID id, Chunk chunk, OfflinePlayer... player)`
- **Purpose**  
  Attempts to attach a chunk to the specified region.
- **Rules enforced**  
    - If the region already owns ≥1 chunk, the new chunk **must be orthogonally adjacent** to one of them.  
    - A failed attempt sends the player message ID `140` ("Not adjacent!").
- **Side-effects**  
    - Adds a `SerializableChunk` to the region.  
    - Fires `ChunkClaimEvent` on the **main thread**.

---

### `unclaimChunk(UUID id, Chunk chunk, OfflinePlayer... player)`
- **Purpose**  
  Public API for **safe** unclaim (split-protection enabled).  
  Delegates to `unclaimChunkInternal(..., force=false)`.

---

### `forceUnclaimChunk(UUID id, Chunk chunk, OfflinePlayer... player)`
- **Purpose**  
  Administrative bypass that **ignores split-protection** and ownership checks.  
  Delegates to `unclaimChunkInternal(..., force=true)`.

---

### `unclaimChunkInternal(UUID id, Chunk chunk, OfflinePlayer[] player, boolean force)`
- **Purpose**  
  Shared implementation for both normal & forced unclaim.  
- **Checks**  
    - `force == false` → runs `wouldSplitRegion(...)`.  
    - If the removal would fracture the region, the executor receives message `141` and the operation aborts.
- **Cleanup**  
    - Removes the chunk from the region **and** any sub-area that spatially contains it.  
    - Optionally schedules **WorldEdit regeneration** (async).  
    - Fires `ChunkUnclaimEvent` (sync).

---

### `removeChunk(UUID id, SerializableChunk chunk)`
- **Purpose**  
  Low-level removal from the region **and** any overlapping sub-areas.  
  (Used internally after all validations passed.)

---

### `wouldSplitRegion(Region region, SerializableChunk chunkToRemove)`
- **Purpose**  
  Graph-algorithm that answers:  
  *"After deleting this chunk, will the remaining chunks stay in one connected piece?"*
- **Algorithm**  
    1. Build an adjacency graph of remaining chunks.  
    2. BFS from an arbitrary chunk.  
    3. If **visited count ≠ total remaining**, the region would split → return **true**.

---

### `areAdjacent(SerializableChunk a, SerializableChunk b)`
- **Purpose**  
  Helper that returns **true** when two chunks are **orthogonally adjacent** (N/S/E/W) **and** in the same world.

---

### `isChunkInDisabledWorld(Chunk chunk)`
- **Purpose**  
  Quick lookup in the config list `disabled-worlds`.

---

### `isChunkClaimed(Chunk chunk)`
- **Purpose**  
  Global check: does **any** region currently own this chunk?

---

### `getRegionOwnsTheChunk(Chunk chunk)`
- **Purpose**  
  Returns the **owning Region** (or **null**) for the supplied chunk.

---

### `hasAdjacentOwnedChunk(Region region, Chunk chunk)`
- **Purpose**  
  Used by `claimChunk` to enforce adjacency.  
  Checks the four direct neighbors; if **any** belongs to the same region → **true**.

---

### `findNearbyUnclaimedChunk(Player player)`
- **Purpose**  
  Spiral search (Manhattan-square expansion) out to **radius 30** looking for the **first unclaimed chunk**.  
  Useful for "give me a spot to claim" commands.

---

### `hasNeighbor(Player player)`
- **Purpose**  
  Returns **true** if any of the four adjacent chunks is **claimed by a DIFFERENT player**.  
  Handy for border-warnings or diplomacy checks.

---

### `getFromLocation(World world, int x, int z)`
- **Purpose**  
  Converts chunk coordinates → `Chunk` object without loading extra chunks.

---

### `getLocation(Player player, Chunk chunk)`
- **Purpose**  
  Finds a **safe standing Y-level** inside the chunk:  
    - Overworld/End → highest block + 2.  
    - Nether → delegates to `findSafeNetherLocation`.  
  Copies pitch/yaw from the player for smooth teleportation.

---

### `getLocation(Player player, SerializableChunk chunk)`
- **Purpose**  
  Overload that works with the serializable wrapper; handles missing/null worlds gracefully.

---

### `findSafeNetherLocation(World world, int x, int z)`
- **Purpose**  
  Simple air-gap scanner between Y=32–127 looking for two consecutive air blocks.  
  Returns **null** if no spot found.

---

### `removeRandomChunk(UUID id)`
- **Purpose**  
  Utility for admin tools or decay systems; **removes one random chunk** from the region (no regeneration).

---

### `deleteInvalidChunks()`
- **Purpose**  
  House-keeping task: iterates every region and purges chunks whose **world no longer exists** on the server.  
  Returns the **count of deleted chunks**.

---
