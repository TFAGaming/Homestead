package tfagaming.projects.minecraft.homestead.integrations.maps;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.flowpowered.math.vector.Vector2i;
import com.technicjelle.BMUtils.Cheese;
import de.bluecolored.bluemap.api.markers.*;
import org.bukkit.Location;
import org.bukkit.World;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;
import tfagaming.projects.minecraft.homestead.structure.Region;
import tfagaming.projects.minecraft.homestead.structure.serializable.SerializableChunk;
import tfagaming.projects.minecraft.homestead.tools.java.Formatters;
import tfagaming.projects.minecraft.homestead.tools.minecraft.chat.ChatColorTranslator;
import tfagaming.projects.minecraft.homestead.tools.minecraft.players.PlayerUtils;

/**
 * Provides BlueMap integration for Homestead regions.
 * <p>
 * Displays each region using {@link ExtrudeMarker}s for its claimed chunks and
 * a {@link POIMarker} for its home location. Compatible with BlueMap API 2.7.6.
 * </p>
 */
public class BlueMapAPI {

    private static final String MARKER_SET_ID = "homestead:regions";
    private final Map<World, MarkerSet> markerSets = new HashMap<>();
    private final de.bluecolored.bluemap.api.BlueMapAPI api;

    /**
     * Creates a new BlueMapAPI handler instance.
     *
     * @param plugin the Homestead plugin instance
     * @param api    the active BlueMap API instance
     */
    public BlueMapAPI(Homestead plugin, de.bluecolored.bluemap.api.BlueMapAPI api) {
        this.api = api;
        update();
    }

    /**
     * Clears only Homestead markers, leaving markers from other plugins untouched.
     */
    public void clearAllMarkers() {
        for (BlueMapMap map : api.getMaps()) {
            MarkerSet set = map.getMarkerSets().get(MARKER_SET_ID);
            if (set != null) set.getMarkers().clear();
        }
    }

    /**
     * Returns the {@link MarkerSet} for the specified world, creating it if necessary.
     *
     * @param world the Bukkit world
     * @return the corresponding marker set
     */
    public MarkerSet getOrNewMarkerSet(World world) {
        MarkerSet markerSet = markerSets.get(world);

        if (markerSet == null) {
            markerSet = MarkerSet.builder()
                    .label("Homestead Regions")
                    .build();
            markerSets.put(world, markerSet);

            final MarkerSet finalMarkerSet = markerSet;
            api.getWorld(world).ifPresent(bmWorld -> {
                for (BlueMapMap map : bmWorld.getMaps()) {
                    map.getMarkerSets().putIfAbsent(MARKER_SET_ID, finalMarkerSet);
                }
            });
        }

        return markerSet;
    }

    /**
     * Creates and adds all map markers for a given region.
     *
     * @param region the region to display on BlueMap
     */
    public void addRegionMarker(Region region) {
        if (region.getChunks().isEmpty()) return;

        boolean isOperator = PlayerUtils.isOperator(region.getOwner());

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{region}", region.getName());
        replacements.put("{region-owner}", region.getOwner().getName());
        replacements.put("{region-members}", ChatColorTranslator.removeColor(
                Formatters.getMembersOfRegion(region), false));
        replacements.put("{region-chunks}", String.valueOf(region.getChunks().size()));
        replacements.put("{global-rank}", String.valueOf(RegionsManager.getGlobalRank(region.getUniqueId())));
        replacements.put("{region-description}", region.getDescription());
        replacements.put("{region-size}", String.valueOf(region.getChunks().size() * 256));

        String hoverTextRaw = Formatters.replace(
                isOperator
                        ? Homestead.config.get("dynamic-maps.chunks.operator-description")
                        : Homestead.config.get("dynamic-maps.chunks.description"),
                replacements
        );

        String plainLabel = region.getName() + " (#" + RegionsManager.getGlobalRank(region.getUniqueId()) + ")";
        plainLabel = ChatColorTranslator.removeColor(plainLabel, false)
                .replaceAll("<[^>]*>", "")
                .replaceAll("&lt;[^&]*&gt;", "")
                .trim();

        String hoverText = hoverTextRaw
                .replaceAll("&lt;", "<")
                .replaceAll("&gt;", ">");

        int chunkColor = region.getMapColor() == 0
                ? (isOperator
                ? Homestead.config.get("dynamic-maps.chunks.operator-color")
                : Homestead.config.get("dynamic-maps.chunks.color"))
                : region.getMapColor();


        MarkerSet markerSet = getOrNewMarkerSet(region.getLocation().getWorld());

        Map<String, Marker> markers = markerSet.getMarkers();

        Vector2i[] chunkCoordinates = region.getChunks().stream().map(chunk -> new Vector2i(chunk.getX(), chunk.getZ())).toArray(Vector2i[]::new);

        Collection<Cheese> platter = Cheese.createPlatterFromChunks(chunkCoordinates);
        int i = 0;
        for (Cheese cheese : platter) {
            ShapeMarker.Builder chunkMarkerBuilder = new ShapeMarker.Builder()
                    .label(region.displayName)
                    .lineColor(new Color(255, 70, 70))
                    .lineWidth(2)
                    .fillColor(new Color(200,70,70))
                    .depthTestEnabled(false)
                    .shape(cheese.getShape(), (float) 90);
            chunkMarkerBuilder.holes(cheese.getHoles().toArray(Shape[]::new));
            ShapeMarker chunkMarker = chunkMarkerBuilder
                    .centerPosition()
                    .build();
            markers.put("towny." + region.getName() + ".area." + i, chunkMarker);
            i += 1;
        }



        addRegionSpawnLocation(markerSet, region, hoverText);
    }

    /**
     * Adds a {@link POIMarker} for the region’s home or spawn location.
     *
     * @param markerSet the marker set the POI will be added to
     * @param region    the region that owns the home
     * @param hoverText the hover text to display for the marker
     */
    public void addRegionSpawnLocation(MarkerSet markerSet, Region region, String hoverText) {
        if (region.getLocation() == null) return;

        Location loc = region.getLocation().getBukkitLocation();
        POIMarker marker = POIMarker.builder()
                .label(region.getName() + " Home")
                .detail(hoverText)
                .position(loc.getX(), loc.getY(), loc.getZ())
                .maxDistance(1000)
                .build();

        String markerId = "region-" + region.getUniqueId() + "-home";
        markerSet.getMarkers().put(markerId, marker);
    }

    /**
     * Rebuilds all Homestead region markers across every world.
     */
    public void update() {
        clearAllMarkers();
        for (Region region : RegionsManager.getAll()) {
            addRegionMarker(region);
        }
    }
}
