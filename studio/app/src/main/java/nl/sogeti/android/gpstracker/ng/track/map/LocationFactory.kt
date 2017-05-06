package nl.sogeti.android.gpstracker.ng.track.map

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.location.LocationManager.GPS_PROVIDER
import android.location.LocationManager.NETWORK_PROVIDER
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber
import java.io.IOException
import java.util.*

class LocationFactory {

    fun getLocationCoordinates(context: Context): LatLng? {
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

    fun getLocationName(context: Context): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        var locality: String? = null
        val latLng = getLocationCoordinates(context)
        if (latLng != null) {
            try {
                val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 3)
                for (address in addresses) {
                    if (address.subLocality != null) {
                        locality = address.subLocality
                        break
                    }
                    if (address.subAdminArea != null) {
                        locality = address.subAdminArea
                        break
                    }
                }
            } catch (exception: IOException) {
                Timber.w(exception, "Failed to retrieved location name")
            }
        }

        return locality
    }
}
