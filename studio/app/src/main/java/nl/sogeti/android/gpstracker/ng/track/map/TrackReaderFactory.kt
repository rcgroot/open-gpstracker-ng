package nl.sogeti.android.gpstracker.ng.track.map

import android.content.Context
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

class TrackReaderFactory {

    fun createTrackReader(context: Context, trackUri: Uri, action: (String, LatLngBounds, List<List<LatLng>>) -> Unit): TrackReader {
        return TrackReader(context, trackUri, action)
    }
}