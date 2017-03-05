package nl.sogeti.android.gpstracker.ng.common.controllers

interface GpsStatusController {

    fun  startUpdates()
    fun  stopUpdates()

    interface Listener {
        fun onStart()
        fun onChange(usedSatellites: Int, maxSatellites: Int)
        fun onFirstFix()
        fun onStop()
    }
}