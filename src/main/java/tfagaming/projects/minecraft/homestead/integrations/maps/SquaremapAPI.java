package tfagaming.projects.minecraft.homestead.integrations.maps;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.marker.*;

public class SquaremapAPI {
    private static Map<World, SimpleLayerProvider> layers = new HashMap<>();
    private static Squaremap api;

    public SquaremapAPI(Homestead plugin) {
        try {
            api = SquaremapProvider.get();
            update();
        } catch (NoClassDefFoundError e) {

        }
    }

    private enum GeoDirection {
        NORTH, EAST, SOUTH, WEST
    }

    public void clearAllMarkers() {
        for (SimpleLayerProvider layer : layers.values()) {
            if (layer != null) {
                layer.getMarkers().removeIf((__) -> true);
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
        SimpleLayerProvider targetLayer = layers.get(world);

        if (targetLayer == null) {
            String layerId = "claims_" + world.getName();
            MapWorld mapWorld = api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).orElse(null);

            if (mapWorld != null) {
                SimpleLayerProvider layer = SimpleLayerProvider.builder("Homestead Regions")
                        .showControls(true)
                        .defaultHidden(false)
                        .layerPriority(1)
                        .zIndex(250)
                        .build();

                mapWorld.layerRegistry().register(Key.of(layerId), layer);
                layers.put(world, layer);
                targetLayer = layer;
            }
        }

        addChunkMarkerWithOptions(targetLayer, chunk, hoverText, chunkColor,
                !isChunkClaimed(region, chunk, GeoDirection.NORTH),
                !isChunkClaimed(region, chunk, GeoDirection.EAST), !isChunkClaimed(region, chunk, GeoDirection.SOUTH),
                !isChunkClaimed(region, chunk, GeoDirection.WEST));

        boolean isEnabled = Homestead.config.get("dynamic-maps.icons.enabled");

        if (isEnabled) {
            final SimpleLayerProvider targetLayerFinal = targetLayer;

            if (region.getLocation() != null
                    && region.getLocation().getBukkitLocation().getChunk().equals(chunk.getBukkitChunk())) {
                Homestead.getInstance().runAsyncTask(() -> {
                    addRegionIcon(targetLayerFinal, region, hoverText);
                });
            }
        }
    }

    private void addRegionIcon(SimpleLayerProvider targetLayer, Region region, String hoverText) {
        BufferedImage bufferedIcon = RegionIconTools.getIconBufferedImage(region.getIcon());

        int iconSize = Homestead.config.get("dynamic-maps.icons.size");

        if (region.getLocation() == null) {
            return;
        }

        if (bufferedIcon != null) {
            try {
                Key iconKey = Key.of("region_icon_" + region.getName().toLowerCase().replaceAll(" ", "_"));

                Location regionLocation = region.getLocation().getBukkitLocation();

                Point iconPoint = Point.of(regionLocation.getX(), regionLocation.getZ());

                if (api.iconRegistry().hasEntry(iconKey) && !api.iconRegistry().get(iconKey).equals(bufferedIcon)) {
                    api.iconRegistry().unregister(iconKey);
                }

                if (!api.iconRegistry().hasEntry(iconKey)) {
                    if (bufferedIcon != null) {
                        api.iconRegistry().register(iconKey, bufferedIcon);
                    }
                }

                Icon icon = Marker.icon(iconPoint, iconKey,
                        iconSize,
                        iconSize);
                MarkerOptions iconOptions = MarkerOptions.builder()
                        .hoverTooltip(hoverText)
                        .build();
                icon.markerOptions(iconOptions);

                targetLayer.addMarker(iconKey, icon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addChunkMarkerWithOptions(SimpleLayerProvider targetLayer,
            SerializableChunk chunk,
            String hoverText,
            int chunkColor,
            boolean north,
            boolean east,
            boolean south,
            boolean west) {
        addChunkMarkerWithOptions(targetLayer, chunk, hoverText, chunkColor, north, east, south, west, 0);
    }

    private void addChunkMarkerWithOptions(SimpleLayerProvider targetLayer,
            SerializableChunk chunk,
            String hoverText,
            int chunkColor,
            boolean north,
            boolean east,
            boolean south,
            boolean west,
            double offset) {
        double minX = chunk.getX() * 16.0;
        double minZ = chunk.getZ() * 16.0;
        double maxX = minX + 16.0;
        double maxZ = minZ + 16.0;

        double offsetMinX = minX + offset;
        double offsetMinZ = minZ + offset;
        double offsetMaxX = maxX - offset;
        double offsetMaxZ = maxZ - offset;

        Point topLeft = Point.of(offsetMinX, offsetMinZ);
        Point topRight = Point.of(offsetMaxX, offsetMinZ);
        Point bottomLeft = Point.of(offsetMinX, offsetMaxZ);
        Point bottomRight = Point.of(offsetMaxX, offsetMaxZ);

        Key chunkKey = Key.of("chunk_" + chunk.getX() + "_" + chunk.getZ());

        Polygon fillArea = Marker.polygon(List.of(
                topLeft, topRight, bottomRight, bottomLeft, topLeft));

        MarkerOptions fillOptions = MarkerOptions.builder()
                .hoverTooltip(hoverText)
                .fillColor(applyAlpha(chunkColor, 50))
                .strokeColor(applyAlpha(chunkColor, 0))
                .strokeWeight(0)
                .fill(true)
                .stroke(false)
                .build();

        fillArea.markerOptions(fillOptions);
        targetLayer.addMarker(Key.of(chunkKey.toString() + "_fill"), fillArea);

        MarkerOptions borderOptions = MarkerOptions.builder()
                .strokeColor(applyAlpha(chunkColor, 255))
                .strokeWeight(2)
                .stroke(true)
                .fill(false)
                .build();

        if (north) {
            Polyline topBorder = Marker.polyline(List.of(topLeft, topRight));
            topBorder.markerOptions(borderOptions);
            targetLayer.addMarker(Key.of(chunkKey.toString() + "_north"), topBorder);
        }

        if (east) {
            Polyline rightBorder = Marker.polyline(List.of(topRight, bottomRight));
            rightBorder.markerOptions(borderOptions);
            targetLayer.addMarker(Key.of(chunkKey.toString() + "_east"), rightBorder);
        }

        if (south) {
            Polyline bottomBorder = Marker.polyline(List.of(bottomLeft, bottomRight));
            bottomBorder.markerOptions(borderOptions);
            targetLayer.addMarker(Key.of(chunkKey.toString() + "_south"), bottomBorder);
        }

        if (west) {
            Polyline leftBorder = Marker.polyline(List.of(topLeft, bottomLeft));
            leftBorder.markerOptions(borderOptions);
            targetLayer.addMarker(Key.of(chunkKey.toString() + "_west"), leftBorder);
        }
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

    public void update() {
        clearAllMarkers();

        for (Region region : RegionsManager.getAll()) {
            for (SerializableChunk chunk : region.getChunks()) {
                addChunkMarker(region, chunk);
            }
        }
    }

    private Color applyAlpha(int color, int alpha) {
        return new Color(
                (color >> 16) & 0xFF,
                (color >> 8) & 0xFF,
                color & 0xFF,
                alpha);
    }
}
