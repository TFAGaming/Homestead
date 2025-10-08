package tfagaming.projects.minecraft.homestead.integrations.maps;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

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

        World world = region.getChunks().get(0).getWorld();
        MarkerSet markerSet = getOrNewMarkerSet(world);

        int index = 0;
        for (SerializableChunk chunk : region.getChunks()) {
            double x1 = chunk.getX() * 16;
            double z1 = chunk.getZ() * 16;
            double x2 = x1 + 16;
            double z2 = z1 + 16;

            Shape shape = Shape.createRect(x1, z1, x2, z2);
            String markerId = "region-" + region.getUniqueId() + "-chunk-" + index++;

            ExtrudeMarker marker = ExtrudeMarker.builder()
                    .label(plainLabel)
                    .detail(hoverText)
                    .depthTestEnabled(false)
                    .shape(shape, -64, 320)
                    .fillColor(new Color(chunkColor, 50))
                    .lineColor(new Color(chunkColor, 255))
                    .lineWidth(1)
                    .build();

            markerSet.getMarkers().put(markerId, marker);
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
