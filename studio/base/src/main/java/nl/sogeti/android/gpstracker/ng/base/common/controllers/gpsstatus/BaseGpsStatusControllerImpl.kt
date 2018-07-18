package nl.sogeti.android.gpstracker.ng.base.common.controllers.gpsstatus

import android.content.Context
import android.location.LocationManager

abstract class BaseGpsStatusControllerImpl(val context: Context, val listener: GpsStatusController.Listener) : GpsStatusController {
    protected val locationManager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun started() {
        listener.onStartListening()
    }

    fun stopped() {
        listener.onStopListening()
    }

    fun firstFix() {
        listener.onFirstFix()
    }

    fun satellites(used: Int, max: Int) {
        listener.onChange(used, max)
    }
}
