package nl.sogeti.android.gpstracker.ng.util

import android.content.Context
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory

class MockGpsStatusControllerFactory : GpsStatusControllerFactory() {

    override fun createGpsStatusController(context: Context, listener: GpsStatusController.Listener): GpsStatusController {

        return MockGpsStatusController(listener)
    }
}