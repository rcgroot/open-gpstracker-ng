package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.content.Context
import android.location.LocationManager
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController

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