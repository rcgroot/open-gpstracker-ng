/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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

import android.content.Context;
import android.databinding.Observable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

import nl.sogeti.android.gpstracker.ng.map.TrackViewModel;
import nl.sogeti.android.gpstracker.v2.R;

public class TrackTileProvider implements TileProvider {
    public static final int STROKE_WIDTH_DP = 2;
    public static final float SPEEDUP_FACTOR = 1f;
    private static final int TILE_SIZE_DP = 256;
    private final float tileSize;
    private final Listener listener;
    private final float strokeWidth;
    private final Bitmap endBitmap;
    private final Bitmap startBitmap;
    private final Callback modelCallback;
    private TrackViewModel track;
    private PathRenderer pathRenderer;

    public TrackTileProvider(Context context, TrackViewModel track, Listener listener) {
        float density = context.getResources().getDisplayMetrics().density;
        float scaleFactor = density * SPEEDUP_FACTOR;
        this.tileSize = TILE_SIZE_DP * scaleFactor;
        this.strokeWidth = STROKE_WIDTH_DP * density;
        this.modelCallback = new Callback();
        this.track = track;
        this.listener = listener;

        track.waypoints.addOnPropertyChangedCallback(modelCallback);
        LatLng[][] wayPoints = track.waypoints.get();
        VectorDrawable startDrawable = (VectorDrawable) context.getDrawable(R.drawable.ic_pin_start_24dp);
        VectorDrawable endDrawable = (VectorDrawable) context.getDrawable(R.drawable.ic_pin_end_24dp);
        startBitmap = renderVectorDrawable(startDrawable);
        endBitmap = renderVectorDrawable(endDrawable);

        pathRenderer = new PathRenderer(tileSize, strokeWidth, wayPoints, startBitmap, endBitmap);
    }

    private Bitmap renderVectorDrawable(VectorDrawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return bitmap;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
//        Timber.d("public Tile getTile(int " + x + ", int " + y + ", int " + zoom + ")");
        Bitmap bitmap = Bitmap.createBitmap((int) tileSize,
                (int) tileSize, android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        pathRenderer.drawPath(canvas, x, y, zoom);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] bitmapData = stream.toByteArray();

        return new Tile((int) tileSize, (int) tileSize, bitmapData);
    }

    public void setTrack(TrackViewModel track) {
        this.track = track;
        track.waypoints.addOnPropertyChangedCallback(modelCallback);
        trackDidChange();
    }

    private void trackDidChange() {
        pathRenderer = new PathRenderer(tileSize, strokeWidth, track.waypoints.get(), startBitmap, endBitmap);
        listener.tilesDidBecomeOutdated(TrackTileProvider.this);
    }

    public interface Listener {
        void tilesDidBecomeOutdated(TrackTileProvider provider);
    }

    private class Callback extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            trackDidChange();
        }
    }
}
