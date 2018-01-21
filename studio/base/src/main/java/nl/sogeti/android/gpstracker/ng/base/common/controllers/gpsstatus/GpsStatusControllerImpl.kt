@file:Suppress("DEPRECATION")

package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.content.Context
import android.location.GpsStatus.*

class GpsStatusControllerImpl(context: Context, listener: GpsStatusController.Listener) : BaseGpsStatusControllerImpl(context, listener) {
    private val callback = object : Listener {
        override fun onGpsStatusChanged(event: Int) {
            when (event) {
                GPS_EVENT_STARTED -> started()
                GPS_EVENT_STOPPED -> stopped()
                GPS_EVENT_FIRST_FIX -> firstFix()
                GPS_EVENT_SATELLITE_STATUS -> {
                    val status = locationManager.getGpsStatus(null)
                    satellites(status.satellites.count(), status.maxSatellites)
                }
            }
        }
    }

    override fun startUpdates() {
        locationManager.addGpsStatusListener(callback)
    }

    override fun stopUpdates() {
        listener.onStop()
        locationManager.removeGpsStatusListener(callback)
    }

}