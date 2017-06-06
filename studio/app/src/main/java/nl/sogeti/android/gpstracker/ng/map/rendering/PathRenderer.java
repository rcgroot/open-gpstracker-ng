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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.VisibleForTesting;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

class PathRenderer {
    private final static int NORMAL_PATH_SIZE = 250;
    private final float strokeWidth;
    private final TileProjection projection;
    private final Point[][] worldPoints;
    private final Bitmap startBitmap;
    private final Bitmap endBitmap;
    private final boolean isLongTrack;

    PathRenderer(float tileSize, float strokeWidth, List<List<LatLng>> wayPoints, Bitmap startBitmap, Bitmap endBitmap) {
        this.strokeWidth = strokeWidth;
        this.startBitmap = startBitmap;
        this.endBitmap = endBitmap;
        if (wayPoints == null) {
            wayPoints = new ArrayList<>();
        }
        projection = new TileProjection(tileSize);
        worldPoints = new Point[wayPoints.size()][];
        int pathLength = 0;
        for (int i = 0; i < wayPoints.size(); i++) {
            final List<LatLng> segmentWayPoint = wayPoints.get(i);
            pathLength += segmentWayPoint.size();
            worldPoints[i] = new Point[segmentWayPoint.size()];
            for (int j = 0; j < segmentWayPoint.size(); j++) {
                worldPoints[i][j] = new Point();
                projection.latLngToWorldCoordinates(segmentWayPoint.get(j), worldPoints[i][j]);
            }
        }
        isLongTrack = pathLength > NORMAL_PATH_SIZE;
    }

    public void drawPath(Canvas canvas, int x, int y, int zoom) {
        Path path = new Path();
        Paint paint = new Paint();
        drawPath(canvas, x, y, zoom, paint, path);
    }

    void drawPath(Canvas canvas, int x, int y, int zoom, Paint paint, Path path) {
        // Loop through all points
        Point first = null, last = null, previous = new Point(), current = new Point();
        for (Point[] worldPoint : worldPoints) {
            projection.worldToTileCoordinates(worldPoint[0], previous, x, y, zoom);
            if (first == null) {
                first = new Point(previous);
            }
            if (worldPoint.length == 1) {
                continue;
            }
            path.moveTo((float) previous.x, (float) previous.y);
            for (int j = 1; j < worldPoint.length; j++) {
                projection.worldToTileCoordinates(worldPoint[j], current, x, y, zoom);
                if (isLongTrack && j < worldPoint.length - 1) {
                    if (completeOffscreen(previous, current, canvas)) {
                        // skips parts with both element offscreen
                        path.moveTo((float) current.x, (float) current.y);
                        Point tmp = previous;
                        previous = current;
                        current = tmp;
                        continue;
                    }
                    if (toCloseTogether(previous, current)) {
                        // or when points are very close together
                        continue;
                    }
                }
                path.lineTo((float) current.x, (float) current.y);
                Point tmp = previous;
                previous = current;
                current = tmp;
            }
            last = new Point(previous);
        }
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(this.strokeWidth);
        paint.setAntiAlias(true);
        paint.setPathEffect(new CornerPathEffect(10));
        canvas.drawPath(path, paint);
        drawPin(canvas, paint, startBitmap, first);
        drawPin(canvas, paint, endBitmap, last);
    }

    private void drawPin(Canvas canvas, Paint paint, Bitmap bitmap, Point point) {
        if (startBitmap != null && point != null) {
            float y = ((float) point.y) - bitmap.getHeight();
            float x = ((float) point.x) - bitmap.getWidth() / 2;
            canvas.drawBitmap(bitmap, x, y, paint);
        }
    }

    private boolean toCloseTogether(Point first, Point second) {
        return first.squaredDistanceTo(second) < 25.0;
    }

    private boolean completeOffscreen(Point first, Point second, Canvas canvas) {
        boolean offScreen = false;
        if (first.y < 0 && second.y < 0) {
            offScreen = true;
        } else if (first.x < 0 && second.x < 0) {
            offScreen = true;
        } else if (first.y > canvas.getHeight() && second.y > canvas.getHeight()) {
            offScreen = true;
        } else if (first.x > canvas.getWidth() && second.x > canvas.getWidth()) {
            offScreen = true;
        }

        return offScreen;
    }

    @VisibleForTesting
    TileProjection getProjection() {
        return projection;
    }
}
