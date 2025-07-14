package tfagaming.projects.minecraft.homestead.integrations.maps;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import net.pl3x.map.core.Pl3xMap;
import net.pl3x.map.core.image.IconImage;
import net.pl3x.map.core.markers.Point;
import net.pl3x.map.core.markers.layer.SimpleLayer;
import net.pl3x.map.core.markers.marker.Marker;
import net.pl3x.map.core.markers.marker.Polyline;
import net.pl3x.map.core.markers.marker.Rectangle;
import net.pl3x.map.core.markers.option.Options;
import net.pl3x.map.core.registry.Registry;
import net.pl3x.map.core.util.Colors;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class Pl3xMapAPI {
    private static Map<World, SimpleLayer> layers = new HashMap<>();

    public Pl3xMapAPI(Homestead plugin) {
        try {
            update();
        } catch (NoClassDefFoundError e) {

        }
    }

    private enum GeoDirection {
        NORTH, EAST, SOUTH, WEST
    }

    public void clearAllMarkers() {
        for (SimpleLayer layer : layers.values()) {
            if (layer != null) {
                layer.getMarkers().removeIf((__) -> {
                    return true;
                });
            }
        }
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

        World world = chunk.getWorld();

        SimpleLayer layer = layers.get(world);
        if (layer == null) {
            String layerId = "claims_" + world.getName();
            Registry<net.pl3x.map.core.world.World> worldRegistry = Pl3xMap.api().getWorldRegistry();
            net.pl3x.map.core.world.World mapWorld = worldRegistry.get(world.getName());

            if (mapWorld != null) {
                layer = new SimpleLayer(layerId, () -> "Homestead Regions");
                layer.setPriority(1);
                layer.setZIndex(1);
                layer.setLiveUpdate(true);

                mapWorld.getLayerRegistry().register(layer);

                layers.put(world, layer);
            }
        }

        addChunkMarkerWithOptions(layer, chunk, hoverText, chunkColor,
                !isChunkClaimed(region, chunk, GeoDirection.NORTH),
                !isChunkClaimed(region, chunk, GeoDirection.EAST), !isChunkClaimed(region, chunk, GeoDirection.SOUTH),
                !isChunkClaimed(region, chunk, GeoDirection.WEST));

        boolean isEnabled = Homestead.config.get("dynamic-maps.icons.enabled");

        if (isEnabled) {
            final SimpleLayer finalLayer = layer;

            if (region.getLocation() != null
                    && region.getLocation().getBukkitLocation().getChunk().equals(chunk.getBukkitChunk())) {
                Homestead.getInstance().runAsyncTask(() -> {
                    addRegionIcon(finalLayer, region, hoverText);
                });
            }
        }
    }

    private void addChunkMarkerWithOptions(SimpleLayer targetLayer,
            SerializableChunk chunk,
            String hoverText,
            int chunkColor,
            boolean north,
            boolean east,
            boolean south,
            boolean west) {

        String markerId = "fill_" + chunk.getX() + "_" + chunk.getZ();
        Point point1 = Point.of(chunk.getX() * 16, chunk.getZ() * 16);
        Point point2 = Point.of(chunk.getX() * 16 + 16, chunk.getZ() * 16 + 16);

        Rectangle rectangle = new Rectangle(markerId, point1, point2);
        rectangle.setOptions(Options.builder()
                .tooltipContent(hoverText)
                .fillColor(Colors.setAlpha(50, chunkColor))
                .strokeColor(Colors.setAlpha(0, chunkColor))
                .strokeWeight(0)
                .fill(true)
                .stroke(false)
                .build());

        targetLayer.addMarker(rectangle);

        if (north) {
            addBorderLine(targetLayer, chunk, GeoDirection.NORTH, chunkColor);
        }

        if (east) {
            addBorderLine(targetLayer, chunk, GeoDirection.EAST, chunkColor);
        }

        if (south) {
            addBorderLine(targetLayer, chunk, GeoDirection.SOUTH, chunkColor);
        }

        if (west) {
            addBorderLine(targetLayer, chunk, GeoDirection.WEST, chunkColor);
        }
    }

    private void addBorderLine(SimpleLayer layer, SerializableChunk chunk, GeoDirection side, int color) {
        int x = chunk.getX();
        int z = chunk.getZ();
        String markerId = "border_" + x + "_" + z + "_" + side;

        Point p1, p2;
        switch (side) {
            case NORTH:
                p1 = Point.of(x * 16, z * 16);
                p2 = Point.of(x * 16 + 16, z * 16);
                break;
            case EAST:
                p1 = Point.of(x * 16 + 16, z * 16);
                p2 = Point.of(x * 16 + 16, z * 16 + 16);
                break;
            case SOUTH:
                p1 = Point.of(x * 16, z * 16 + 16);
                p2 = Point.of(x * 16 + 16, z * 16 + 16);
                break;
            case WEST:
                p1 = Point.of(x * 16, z * 16);
                p2 = Point.of(x * 16, z * 16 + 16);
                break;
            default:
                return;
        }

        Polyline line = new Polyline(markerId, List.of(p1, p2));
        line.setOptions(Options.builder()
                .strokeColor(Colors.setAlpha(255, color))
                .strokeWeight(2)
                .build());

        layer.addMarker(line);
    }

    public boolean isChunkClaimed(Region region, SerializableChunk chunk, GeoDirection direction) {
        int x = chunk.getX();
        int z = chunk.getZ();
        World world = chunk.getWorld();

        boolean result;
        switch (direction) {
            case NORTH: {
                Region chunksRegion = ChunksManager
                        .getRegionOwnsTheChunk(new SerializableChunk(world, x, z - 1).getBukkitChunk());

                result = chunksRegion != null && chunksRegion.getUniqueId().equals(region.getUniqueId());
                break;
            }
            case EAST: {
                Region chunksRegion = ChunksManager
                        .getRegionOwnsTheChunk(new SerializableChunk(world, x + 1, z).getBukkitChunk());

                result = chunksRegion != null && chunksRegion.getUniqueId().equals(region.getUniqueId());
                break;
            }
            case SOUTH: {
                Region chunksRegion = ChunksManager
                        .getRegionOwnsTheChunk(new SerializableChunk(world, x, z + 1).getBukkitChunk());

                result = chunksRegion != null && chunksRegion.getUniqueId().equals(region.getUniqueId());
                break;
            }
            case WEST: {
                Region chunksRegion = ChunksManager
                        .getRegionOwnsTheChunk(new SerializableChunk(world, x - 1, z).getBukkitChunk());

                result = chunksRegion != null && chunksRegion.getUniqueId().equals(region.getUniqueId());
                break;
            }
            default:
                result = false;
        }

        return result;
    }

    private void addRegionIcon(SimpleLayer layer, Region region, String hoverText) {
        BufferedImage bufferedIcon = RegionIconTools.getIconBufferedImage(region.getIcon());

        int iconSize = Homestead.config.get("dynamic-maps.icons.size");

        if (region.getLocation() == null) {
            return;
        }

        try {
            String iconId = "region_icon_" + region.getName().toLowerCase().replaceAll(" ", "_");
            Location regionLocation = region.getLocation().getBukkitLocation();
            Point iconPoint = Point.of(regionLocation.getX(), regionLocation.getZ());

            if (bufferedIcon != null) {
                IconImage iconImage = new IconImage(iconId, bufferedIcon, "png");

                if (Pl3xMap.api().getIconRegistry().has(iconId)
                        && !Pl3xMap.api().getIconRegistry().get(iconId).getImage().equals(iconImage.getImage())) {
                    Pl3xMap.api().getIconRegistry().unregister(iconId);
                }

                if (!Pl3xMap.api().getIconRegistry().has(iconId)) {
                    Pl3xMap.api().getIconRegistry().register(iconId, iconImage);
                }

                Marker<?> iconMarker = Marker.icon(
                        "marker_" + iconId,
                        iconPoint,
                        iconId,
                        iconSize, iconSize);

                iconMarker.setOptions(Options.builder()
                        .tooltipContent(hoverText)
                        .build());

                layer.addMarker(iconMarker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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