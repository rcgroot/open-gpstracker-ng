package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.annotation.TargetApi
import android.content.Context
import android.location.GnssStatus
import android.os.Build.VERSION_CODES.N

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