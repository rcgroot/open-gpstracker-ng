package nl.sogeti.android.gpstracker.map.rendering;

import android.content.Context;
import android.databinding.Observable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.VectorDrawable;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

import nl.sogeti.android.gpstracker.map.TrackViewModel;
import nl.sogeti.android.gpstracker.v2.R;

public class TrackTileProvider implements TileProvider {
    public static final int STROKE_WIDTH_DP = 2;
    private static final int TILE_SIZE_DP = 256;
    public static final float SPEEDUP_FACTOR = 1f;
    private final float scaleFactor;
    private final float tileSize;
    private TrackViewModel track;
    private final Listener listener;
    private final Observable.OnPropertyChangedCallback modelCallback;
    private final float strokeWidth;
    private final Bitmap endBitmap;
    private final Bitmap startBitmap;
    private PathRenderer pathRenderer;

    public TrackTileProvider(Context context, TrackViewModel track, Listener listener) {
        float density = context.getResources().getDisplayMetrics().density;
        scaleFactor = density * SPEEDUP_FACTOR;
        this.track = track;
        this.listener = listener;

        tileSize = TILE_SIZE_DP * scaleFactor;
        modelCallback = new Callback();
        track.waypoints.addOnPropertyChangedCallback(modelCallback);
        LatLng[][] wayPoints = track.waypoints.get();
        strokeWidth = STROKE_WIDTH_DP * density;
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
