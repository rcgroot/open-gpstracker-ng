package nl.sogeti.android.gpstracker.ng.util

import android.content.Context
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerProvider

class MockGpsStatusControllerProvider : GpsStatusControllerProvider() {

    override fun createGpsStatusListenerProvider(context: Context, listener: GpsStatusController.Listener): GpsStatusController {

        return MockGpsStatusController(listener)
    }
}