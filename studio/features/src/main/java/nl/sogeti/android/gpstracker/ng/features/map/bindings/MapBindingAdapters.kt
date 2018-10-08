package nl.sogeti.android.gpstracker.ng.features.map.bindings

import androidx.databinding.BindingAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import nl.sogeti.android.gpstracker.ng.base.location.LatLng
import nl.sogeti.android.opengpstrack.ng.features.R

open class MapBindingAdapters {

    @BindingAdapter("polylines")
    fun setPolylines(map: MapView, polylines: List<PolylineOptions>?) {
        val addLines: (GoogleMap) -> Unit = { googleMap ->
            googleMap.uiSettings.isMapToolbarEnabled = false
            map.tag = googleMap
            googleMap.clear()
            polylines?.map {
                googleMap.addPolyline(it)
            }
        }
        val tag = map.tag
        if (tag is GoogleMap) {
            addLines(tag)
        } else {
            map.getMapAsync(addLines)
        }
    }

    @BindingAdapter("bounds")
    fun setMapBounds(map: MapView, bounds: LatLngBounds?) {
        if (bounds != null) {
            map.getMapAsync {
                val padding = map.context.resources.getDimension(R.dimen.map_padding)
                val update = CameraUpdateFactory.newLatLngBounds(bounds, padding.toInt())
                it.animateCamera(update, null)
            }
        }
    }

    @BindingAdapter("center")
    fun setMapTarget(map: MapView, center: LatLng?) {
        if (center != null) {
            map.getMapAsync {
                val cameraFocus = com.google.android.gms.maps.model.LatLng(center.latitude, center.longitude)
                val update = if (it.cameraPosition.zoom < OVERVIEW || it.cameraPosition.zoom > CLOSE_UP) {
                    CameraUpdateFactory.newLatLngZoom(cameraFocus, CLOSE_UP)
                } else {
                    CameraUpdateFactory.newLatLng(cameraFocus)
                }
                it.animateCamera(update)
            }
        }
    }

    @BindingAdapter("satellite")
    fun setSatellite(map: MapView, showSatellite: Boolean) {
        map.getMapAsync {
            if (showSatellite) {
                it.mapType = MAP_TYPE_HYBRID
            } else {
                it.mapType = MAP_TYPE_NORMAL
            }
        }
    }

    companion object {
        private const val OVERVIEW = 10.0F
        private const val CLOSE_UP = 15.0F
    }
}
