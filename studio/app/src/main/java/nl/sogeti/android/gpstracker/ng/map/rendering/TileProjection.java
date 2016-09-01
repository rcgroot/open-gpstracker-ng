/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.map.rendering;

import android.support.annotation.VisibleForTesting;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Reworked code found on OpenStreetMap Wiki and Stack Overflow
 * <p/>
 *
 * @see <a href="https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames">openstreetmap</a>
 * @see <a href="https://stackoverflow.com/questions/20382823/google-maps-api-v2-draw-part-of-circle-on-mapfragment">stackoverflow</a>
 */
class TileProjection {

    private static final double MAX_PIXEL_COORDINATE = 0.9999;
    private static final double MIN_PIXEL_COORDINATE = -0.9999;
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

    /**
     * Based on work from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
     *
     * @param y tile coordinate system Y
     * @param z tile coordinate system zoom
     * @return latitude
     * @license Creative Commons Attribution-ShareAlike 2.0 license
     */
    private static double tileToLatitude(int y, int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    /**
     * Based on work from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
     *
     * @param x tile coordinate system X
     * @param z tile coordinate system zoom
     * @return longitude
     * @license Creative Commons Attribution-ShareAlike 2.0 license
     */
    private static double tileToLongitude(int x, int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    /**
     * Builds a latitude/longitude bounding box for a tile
     * <p/>
     * Based on work from: https://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
     *
     * @param x    tile coordinate system X
     * @param y    tile coordinate system Y
     * @param zoom tile coordinate system zoom
     * @return
     * @license Creative Commons Attribution-ShareAlike 2.0 license
     */
    static LatLngBounds tileBounds(int x, int y, int zoom) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng northWest = new LatLng(tileToLatitude(y, zoom), tileToLongitude(x, zoom));
        LatLng southEast = new LatLng(tileToLatitude(y + 1, zoom), tileToLongitude(x + 1, zoom));
        builder.include(northWest).include(southEast);

        return builder.build();
    }

    @VisibleForTesting
    float getTileSize() {
        return tileSize;
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
        double sinY = bound(Math.sin(Math.toRadians(latLng.latitude)));
        worldPoint.y = origin.y + 0.5f * (Math.log((1 + sinY) / (1 - sinY)) * -pixelsPerLonRadian);
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
     * @return value bound between min and max
     */
    private double bound(double value) {
        value = Math.max(value, MIN_PIXEL_COORDINATE);
        value = Math.min(value, MAX_PIXEL_COORDINATE);

        return value;
    }
}
