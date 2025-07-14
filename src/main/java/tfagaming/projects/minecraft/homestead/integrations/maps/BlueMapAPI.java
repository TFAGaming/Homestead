package tfagaming.projects.minecraft.homestead.integrations.maps;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ExtrudeMarker;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class BlueMapAPI {
    private Map<World, MarkerSet> markerSets = new HashMap<>();
    public final de.bluecolored.bluemap.api.BlueMapAPI api;

    public BlueMapAPI(Homestead plugin, de.bluecolored.bluemap.api.BlueMapAPI api) {
        this.api = api;

        update();
    }

    public void clearAllMarkers() {
        for (MarkerSet markerSet : markerSets.values()) {
            markerSet.getMarkers().clear();
        }
    }

    public MarkerSet getOrNewMarkerSet(World world) {
        MarkerSet markerSet = markerSets.get(world);

        if (markerSet == null) {
            markerSet = MarkerSet.builder()
                    .label("Homestead Regions")
                    .build();

            markerSets.put(world, markerSet);
        }

        return markerSet;
    }

    public void addChunkMarker(Region region, SerializableChunk chunk) {
        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("{region}", region.getName());
        replacements.put("{region-owner}", region.getOwner().getName());
        replacements.put("{region-members}",
                ChatColorTranslator.removeColor(Formatters.getMembersOfRegion(region), false));
        replacements.put("{region-chunks}", String.valueOf(region.getChunks().size()));
        replacements.put("{global-rank}", String.valueOf(RegionsManager.getGlobalRank(region.getUniqueId())));
        replacements.put("{region-description}", region.getDescription());
        replacements.put("{region-size}", String.valueOf(region.getChunks().size() * 256));

        boolean isOperator = PlayerUtils.isOperator(region.getOwner());

        String hoverText = Formatters
                .replace(isOperator ? Homestead.config.get("dynamic-maps.chunks.operator-description")
                        : Homestead.config.get("dynamic-maps.chunks.description"), replacements);

        int chunkColor = region.getMapColor() == 0
                ? (isOperator ? Homestead.config.get("dynamic-maps.chunks.operator-color")
                        : Homestead.config.get("dynamic-maps.chunks.color"))
                : region.getMapColor();

        MarkerSet markerSet = getOrNewMarkerSet(chunk.getWorld());

        String markerId = "chunk_" + chunk.getX() + "_" + chunk.getZ();

        Location loc1 = new Location(chunk.getWorld(), chunk.getX() * 16, 0, chunk.getZ() * 16);
        Location loc2 = new Location(chunk.getWorld(), (chunk.getX() * 16) + 16, 0, (chunk.getZ() * 16) + 16);

        Shape shape = Shape.createRect(
                loc1.getX(),
                loc1.getZ(),
                loc2.getX(),
                loc2.getZ());

        ExtrudeMarker marker = ExtrudeMarker.builder()
                .label(hoverText)
                .detail(hoverText)
                .depthTestEnabled(false)
                .shape(shape, -64, 320)
                .position(loc1.getX(), -64, loc1.getZ())
                .fillColor(new Color(chunkColor, 50))
                .lineColor(new Color(chunkColor, 255))
                .lineWidth(1)
                .build();

        markerSet.getMarkers().put(markerId, marker);

        addRegionSpawnLocation(markerSet, region, hoverText);

        for (Player player : Bukkit.getOnlinePlayers()) {
            api.getWorld(player.getWorld()).ifPresent(world -> {
                for (BlueMapMap map : world.getMaps()) {
                    map.getMarkerSets().clear();

                    map.getMarkerSets().put("region-" + region.getUniqueId(), markerSet);
                }
            });
        }
    }

    public void addRegionSpawnLocation(MarkerSet markerSet, Region region, String hoverText) {
        if (region.getLocation() == null) {
            return;
        }

        Location location = region.getLocation().getBukkitLocation();

        POIMarker marker = POIMarker.builder()
                .label(hoverText)
                .position(location.getX(), location.getY(), location.getZ())
                .maxDistance(1000)
                .build();

        markerSet.getMarkers()
                .put("region-" + region.getUniqueId(), marker);
    }

    public void update() {
        clearAllMarkers();

        for (Region region : RegionsManager.getAll()) {
            for (SerializableChunk chunk : region.getChunks()) {
                addChunkMarker(region, chunk);
            }
        }
    }
}