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
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import nl.sogeti.android.gpstracker.ng.utils.CommonKt;
import nl.sogeti.android.gpstracker.v2.R;

public class TrackTileProvider implements TileProvider {
    private static final int STROKE_WIDTH_DP = 2;
    private static final float SPEEDUP_FACTOR = 1f;
    private static final int TILE_SIZE_DP = 256;

    private final float tileSize;
    private final float strokeWidth;
    private final Bitmap endBitmap;
    private final Bitmap startBitmap;
    private PathRenderer pathRenderer;
    private TileOverlay titleOverLay;
    private ObservableField<List<List<LatLng>>> waypoints;
    private Observable.OnPropertyChangedCallback modelCallback = new Callback();

    public TrackTileProvider(Context context, ObservableField<List<List<LatLng>>> waypoints) {
        float density = context.getResources().getDisplayMetrics().density;
        float scaleFactor = density * SPEEDUP_FACTOR;
        this.tileSize = TILE_SIZE_DP * scaleFactor;
        this.strokeWidth = STROKE_WIDTH_DP * density;

        VectorDrawableCompat startDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_pin_start_24dp, null);
        VectorDrawableCompat endDrawable = VectorDrawableCompat.create(context.getResources(), R.drawable.ic_pin_end_24dp, null);
        startBitmap = renderVectorDrawable(startDrawable);
        endBitmap = renderVectorDrawable(endDrawable);

        setWaypoints(waypoints);
    }

    private Bitmap renderVectorDrawable(Drawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        return bitmap;
    }

    @Override
    public Tile getTile(int x, int y, int zoom) {
        Bitmap bitmap = Bitmap.createBitmap((int) tileSize,
                (int) tileSize, android.graphics.Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        pathRenderer.drawPath(canvas, x, y, zoom);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        byte[] bitmapData = stream.toByteArray();

        return new Tile((int) tileSize, (int) tileSize, bitmapData);
    }

    public void setWaypoints(ObservableField<List<List<LatLng>>> waypoints) {
        if (this.waypoints != null) {
            this.waypoints.removeOnPropertyChangedCallback(modelCallback);
        }
        this.waypoints = waypoints;
        waypoints.addOnPropertyChangedCallback(modelCallback);
        waypointsDidChange();
    }

    private void waypointsDidChange() {
        pathRenderer = new PathRenderer(tileSize, strokeWidth, waypoints.get(), startBitmap, endBitmap);
        if (titleOverLay != null) {
            //Like: executeOnUiThread { titleOverLay.clearTileCache() }
            CommonKt.executeOnUiThread(new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    titleOverLay.clearTileCache();
                    return Unit.INSTANCE;
                }
            });
        }
    }

    public void provideFor(@NotNull GoogleMap googleMap) {
        TileOverlayOptions options = new TileOverlayOptions();
        options.tileProvider(this);
        options.fadeIn(true);
        titleOverLay = googleMap.addTileOverlay(options);
    }

    private class Callback extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            waypointsDidChange();
        }
    }
}
