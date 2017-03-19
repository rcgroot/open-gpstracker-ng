package nl.sogeti.android.gpstracker.ng.track.map.rendering

import android.content.Context
import android.databinding.ObservableField
import com.google.android.gms.maps.model.LatLng

class TrackTileProviderFactory {

    fun createTrackTileProvider(context: Context, waypoints: ObservableField<List<List<LatLng>>>) = TrackTileProvider(context, waypoints)
}