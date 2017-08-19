/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.ng.map

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

class LocationFactoryImpl : LocationFactory {

    override fun getLocationCoordinates(context: Context): LatLng? {
        val locationService = context.getSystemService(LOCATION_SERVICE) as? LocationManager
        var location: LatLng? = null
        try {
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
            if (lastKnownLocation != null) {
                location = LatLng(lastKnownLocation.latitude, lastKnownLocation.longitude)
            }
        } catch (e: SecurityException) {
            Timber.e(e, "Last known location not found, missing permission.")
        }

        return location
    }

    override fun getLocationName(context: Context): String? {
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
