@file:Suppress("DEPRECATION")

package nl.sogeti.android.gpstracker.ng.common.controllers

import android.annotation.TargetApi
import android.content.Context
import android.location.GnssStatus
import android.location.GpsStatus
import android.location.GpsStatus.*
import android.location.LocationManager
import android.os.Build
import android.os.Build.VERSION_CODES.N

open class GpsStatusControllerProvider {

    open fun createGpsStatusListenerProvider(context: Context, listener: GpsStatusController.Listener): GpsStatusController {
        val controller: GpsStatusController
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            controller = GnnsStatusControllerImpl(context, listener)
        } else {
            controller = GpsStatusControllerImpl(context, listener)
        }

        return controller
    }
}

abstract class BaseGpsStatusControllerImpl(val context: Context, val listener: GpsStatusController.Listener) : GpsStatusController {
    val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun started() {
        listener.onStart()
    }

    fun stopped() {
        listener.onStop()
    }

    fun firstFix() {
        listener.onFirstFix()
    }

    fun satellites(used: Int, max: Int) {
        listener.onChange(used, max)
    }
}

class GpsStatusControllerImpl(context: Context, listener: GpsStatusController.Listener) : BaseGpsStatusControllerImpl(context, listener) {
    private val callback = object : GpsStatus.Listener {
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
        locationManager.addGpsStatusListener(callback)
    }

}

@TargetApi(N)
class GnnsStatusControllerImpl(context: Context, listener: GpsStatusController.Listener) : BaseGpsStatusControllerImpl(context, listener) {
    private val callback = object : GnssStatus.Callback() {
        override fun onStarted() {
            started()
        }

        override fun onStopped() {
            stopped()
        }

        override fun onFirstFix(ttffMillis: Int) {
            firstFix()
        }

        override fun onSatelliteStatusChanged(status: GnssStatus) {
            val used = (0..status.satelliteCount).count { status.usedInFix(it) }
            satellites(used, status.satelliteCount)
        }
    }

    override fun startUpdates() {
        locationManager.registerGnssStatusCallback(callback)
    }

    override fun stopUpdates() {
        listener.onStop()
        locationManager.unregisterGnssStatusCallback(callback)
    }

}
