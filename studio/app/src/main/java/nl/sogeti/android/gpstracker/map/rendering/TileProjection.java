package nl.sogeti.android.gpstracker.map.rendering;

import android.support.annotation.VisibleForTesting;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Reworked code found on OpenStreetMap Wiki and Stack Overflow
 * <p/>
 * https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
 * https://stackoverflow.com/questions/20382823/google-maps-api-v2-draw-part-of-circle-on-mapfragment
 */
public class TileProjection {

    private final float tileSize;

    private final Point origin;
    private final double pixelsPerLonRadian;
    private final double pixelsPerLonDegree;
    TileProjection(float tileSize) {
        this.tileSize = tileSize;
        this.origin = new Point(tileSize / 2.0f, tileSize / 2.0f);
        this.pixelsPerLonDegree = tileSize / 360d;
        this.pixelsPerLonRadian = tileSize / (2 * Math.PI);
    }

    @VisibleForTesting
    float getTileSize() {
        return tileSize;
    }

    /**
     * Based on work from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
     *
     * @param y
     * @param z
     * @return
     * @license Creative Commons Attribution-ShareAlike 2.0 license
     */
    static double tileTolatitude(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    /**
     * Based on work from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
     *
     * @param x
     * @param z
     * @return
     * @license Creative Commons Attribution-ShareAlike 2.0 license
     */
    static double tileToLongitude(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    /**
     * Builds a latitude/longitude bounding box for a tile
     * <p/>
     * Based on work from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
     *
     * @param x
     * @param y
     * @param zoom
     * @return
     * @license Creative Commons Attribution-ShareAlike 2.0 license
     */
    static LatLngBounds tileBounds(int x, int y, int zoom) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng northWest = new LatLng(tileTolatitude(y, zoom), tileToLongitude(x, zoom));
        LatLng southEast = new LatLng(tileTolatitude(y + 1, zoom), tileToLongitude(x + 1, zoom));
        builder.include(northWest).include(southEast);

        return builder.build();
    }

    /**
     * Stack overflow projection code from Latitude/Longitude to tile pixel coordinates
     *
     * @param latLng    in parameter
     * @param tilePoint out parameter with the result
     */
    public void latLngToPoint(LatLng latLng, Point tilePoint, int x, int y, int zoom) {
        latLngToWorldCoordinates(latLng, tilePoint);
        worldToTileCoordinates(tilePoint, tilePoint, x, y, zoom);
    }

    /**
     * Stack overflow projection code from Latitude/Longitude to tile pixel coordinates
     *
     * @param latLng
     * @param worldPoint out parameter with the result
     */
    public void latLngToWorldCoordinates(LatLng latLng, Point worldPoint) {
        worldPoint.x = origin.x + latLng.longitude * pixelsPerLonDegree;
        double siny = bound(Math.sin(Math.toRadians(latLng.latitude)), -0.9999, 0.9999);
        worldPoint.y = origin.y + 0.5f * (Math.log((1 + siny) / (1 - siny)) * -pixelsPerLonRadian);
    }

    /**
     * Stack overflow projection code from Latitude/Longitude to tile pixel coordinates
     *
     * @param worldPoint
     * @param tilePoint  out parameter with the result
     */
    public void worldToTileCoordinates(Point worldPoint, Point tilePoint, int x, int y, int zoom) {
        int numTiles = 1 << zoom;
        tilePoint.x = worldPoint.x * numTiles;
        tilePoint.y = worldPoint.y * numTiles;
        tilePoint.x -= x * tileSize;
        tilePoint.y -= y * tileSize;
    }

    /**
     * Stack overflow projection code from Latitude/Longitude to tile pixel coordinates
     *
     * @param value
     * @param min
     * @param max
     * @return value bound between min and max
     */
    private double bound(double value, double min, double max) {
        value = Math.max(value, min);
        value = Math.min(value, max);

        return value;
    }
}
