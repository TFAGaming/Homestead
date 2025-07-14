package tfagaming.projects.minecraft.homestead.integrations.maps;

import java.util.HashMap;

import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.integrations.maps.listeners.DynmapListener;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

public class DynmapAPI {
    public static MarkerSet markerSet;

    public DynmapAPI(Homestead plugin) {
        try {
            DynmapCommonAPIListener.register(new DynmapListener());
        } catch (NoClassDefFoundError e) {

        }
    }

    public void clearAllMarkers() {
        if (markerSet == null) {
            return;
        }

        for (AreaMarker marker : markerSet.getAreaMarkers()) {
            marker.deleteMarker();
        }
    }

    public void addChunkMarker(Region region, SerializableChunk chunk) {
        if (markerSet == null) {
            return;
        }

        double[] x = { chunk.getX() * 16, (chunk.getX() + 1) * 16 };
        double[] z = { chunk.getZ() * 16, (chunk.getZ() + 1) * 16 };

        String markerId = "claimed_" + chunk.getWorldName() + "_" + chunk.getX() + "_" + chunk.getZ();

        AreaMarker existingMarker = markerSet.findAreaMarker(markerId);

        if (existingMarker != null) {
            return;
        }

        String markerLabel = region.getName();

        AreaMarker areaMarker = markerSet.createAreaMarker(
                markerId,
                markerLabel,
                false,
                chunk.getWorldName(),
                x,
                z,
                false);

        if (areaMarker == null) {
            return;
        }

        boolean isOperator = PlayerUtils.isOperator(region.getOwner());

        int chunkColor = region.getMapColor() == 0
                ? (isOperator ? Homestead.config.get("dynamic-maps.chunks.operator-color")
                        : Homestead.config.get("dynamic-maps.chunks.color"))
                : region.getMapColor();

        areaMarker.setLineStyle(1, 0.8, chunkColor);
        areaMarker.setFillStyle(0.10, chunkColor);

        HashMap<String, String> replacements = new HashMap<>();

        replacements.put("{region}", region.getName());
        replacements.put("{region-owner}", region.getOwner().getName());
        replacements.put("{region-members}",
                ChatColorTranslator.removeColor(Formatters.getMembersOfRegion(region), false));
        replacements.put("{region-chunks}", String.valueOf(region.getChunks().size()));
        replacements.put("{global-rank}", String.valueOf(RegionsManager.getGlobalRank(region.getUniqueId())));
        replacements.put("{region-description}", region.getDescription());
        replacements.put("{region-size}", String.valueOf(region.getChunks().size() * 256));

        String description = Formatters
                .replace(isOperator ? Homestead.config.get("dynamic-maps.chunks.operator-description")
                        : Homestead.config.get("dynamic-maps.chunks.description"), replacements);

        areaMarker.setDescription(description);
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
