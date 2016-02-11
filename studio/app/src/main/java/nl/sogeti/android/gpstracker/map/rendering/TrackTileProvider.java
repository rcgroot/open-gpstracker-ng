package nl.sogeti.android.gpstracker.map.rendering;

import android.content.Context;
import android.databinding.Observable;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileProvider;

import java.io.ByteArrayOutputStream;

import nl.sogeti.android.gpstracker.map.TrackViewModel;

public class TrackTileProvider implements TileProvider {
    private static final int TILE_SIZE_DP = 256;
    private final float scaleFactor;
    private final float tileSize;
    private final TrackViewModel track;
    private final Listener listener;
    private final Observable.OnPropertyChangedCallback modelCallback;
    private PathRenderer pathRenderer;

    public TrackTileProvider(Context context, TrackViewModel track, Listener listener) {
        scaleFactor = context.getResources().getDisplayMetrics().density * 0.6f;
        this.track = track;
        this.listener = listener;

        tileSize = TILE_SIZE_DP * scaleFactor;
        modelCallback = new Callback();
        track.waypoints.addOnPropertyChangedCallback(modelCallback);
        LatLng[] wayPoints = track.waypoints.get();
        pathRenderer = new PathRenderer(tileSize, wayPoints);
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


    public interface Listener {
        void tilesDidBecomeOutdated(TrackTileProvider provider);
    }

    private class Callback extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            pathRenderer = new PathRenderer(tileSize, track.waypoints.get());
            listener.tilesDidBecomeOutdated(TrackTileProvider.this);
        }
    }
}
