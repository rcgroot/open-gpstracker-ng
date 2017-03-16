@file:Suppress("DEPRECATION")

package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.content.Context
import android.os.Build
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController

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

