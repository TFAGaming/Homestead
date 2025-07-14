package tfagaming.projects.minecraft.homestead.integrations.maps.listeners;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import tfagaming.projects.minecraft.homestead.integrations.maps.DynmapAPI;
import tfagaming.projects.minecraft.homestead.logs.Logger;

public class DynmapListener extends DynmapCommonAPIListener {
    @Override
    public void apiEnabled(DynmapCommonAPI api) {
        MarkerAPI markerAPI = api.getMarkerAPI();

        if (markerAPI == null) {
            Logger.error("Failed to load dynmap API.");
            return;
        }

        MarkerSet markerSet = markerAPI.createMarkerSet(
                "claimedChunks",
                "Homestead Regions",
                null,
                false);

        if (markerSet == null) {
            Logger.error("Failed to load dynmap API.");
            return;
        }

        markerSet.setLayerPriority(10);
        markerSet.setHideByDefault(false);

        DynmapAPI.markerSet = markerSet;
    }
}