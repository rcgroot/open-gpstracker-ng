package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import nl.sogeti.android.gpstracker.ng.base.common.controllers.gpsstatus.GnnsStatusControllerImpl
import nl.sogeti.android.gpstracker.ng.base.common.controllers.gpsstatus.GpsStatusController

open class GpsStatusControllerFactory(val context: Context) {

    open fun createGpsStatusController(listener: GpsStatusController.Listener): GpsStatusController {
        return createGpsStatusController(context, listener, Build.VERSION.SDK_INT)
    }

    @SuppressLint("NewApi")
    @Suppress("DEPRECATION")
    fun createGpsStatusController(context: Context, listener: GpsStatusController.Listener, sdkVersion: Int): GpsStatusController {
        val controller: GpsStatusController
        if (sdkVersion >= Build.VERSION_CODES.N) {
            controller = GnnsStatusControllerImpl(context, listener)
        } else {
            controller = GpsStatusControllerImpl(context, listener)
        }

        return controller
    }
}
