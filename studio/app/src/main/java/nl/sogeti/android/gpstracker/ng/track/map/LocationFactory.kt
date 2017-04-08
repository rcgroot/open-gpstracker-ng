package nl.sogeti.android.gpstracker.ng.track.map

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import com.google.android.gms.maps.model.LatLng

class LocationFactory {

    fun getLocation(context: Context): LatLng? {
        val locationService = context.getSystemService(LOCATION_SERVICE) as? LocationManager
        val gpsLocation = locationService?.getLastKnownLocation(GPS_PROVIDER)
        val networkLocation = locationService?.getLastKnownLocation(NETWORK_PROVIDER)
        var lastKnownLocation: Location?
        if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.time > networkLocation.time) {
                lastKnownLocation = gpsLocation
            } else {
                lastKnownLocation = networkLocation
            }
        } else {
            lastKnownLocation = gpsLocation ?: networkLocation
        }
        var location: LatLng? = null
        if (lastKnownLocation != null) {
            location = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
        }
        return location
    }

}