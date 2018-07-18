package nl.sogeti.android.gpstracker.ng.base.common.controllers.gpsstatus

interface GpsStatusController {

    fun  startUpdates()
    fun  stopUpdates()

    interface Listener {
        fun onStartListening()
        fun onChange(usedSatellites: Int, maxSatellites: Int)
        fun onFirstFix()
        fun onStopListening()
    }
}
