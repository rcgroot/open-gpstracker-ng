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
package nl.sogeti.android.gpstracker.map.rendering;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.maps.model.LatLng;

public class PathRenderer {
    private final float strokeWidth;
    private TileProjection projection;

    private Point[][] worldPoints;

    public PathRenderer(float tileSize, float strokeWidth, LatLng[][] wayPoints) {
        this.strokeWidth = strokeWidth;
        if (wayPoints == null) {
            wayPoints = new LatLng[0][0];
        }
        projection = new TileProjection(tileSize);
        worldPoints = new Point[wayPoints.length][];
        for (int i = 0; i < wayPoints.length; i++) {
            worldPoints[i] = new Point[wayPoints[i].length];
            for (int j = 0; j < wayPoints[i].length; j++) {
                worldPoints[i][j] = new Point();
                projection.latLngToWorldCoordinates(wayPoints[i][j], worldPoints[i][j]);
            }
        }
    }

    @VisibleForTesting
    TileProjection getProjection() {
        return projection;
    }

    public void drawPath(Canvas canvas, int x, int y, int zoom) {
        Paint paint = new Paint();
        Path path = new Path();
        // Loop through all points, skips parts with both element offscreen
        // or when points are very close together
        Point previous = new Point();
        Point current = new Point();
        for (int i = 0; i < worldPoints.length; i++) {
            if (worldPoints[i].length <= 1) {
                continue;
            }
            projection.worldToTileCoordinates(worldPoints[i][0], previous, x, y, zoom);
            path.moveTo((float) previous.x, (float) previous.y);
            for (int j = 1; j < worldPoints[i].length; j++) {
                projection.worldToTileCoordinates(worldPoints[i][j], current, x, y, zoom);
                if (!completeOffscreen(previous, current) || !toCloseTogether(previous, current)) {
                    path.lineTo((float) current.x, (float) current.y);
                    Point tmp = previous;
                    previous = current;
                    current = tmp;
                }
            }
        }
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(this.strokeWidth);
        paint.setAntiAlias(true);
        paint.setPathEffect(new CornerPathEffect(10));
        canvas.drawPath(path, paint);
        path.rewind();
    }

    @VisibleForTesting
    boolean toCloseTogether(Point first, Point seconds) {
        return false;
    }

    @VisibleForTesting
    boolean completeOffscreen(Point first, Point seconds) {
        return false;
    }
}
