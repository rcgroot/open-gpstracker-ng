package nl.sogeti.android.gpstracker.service.integration

import android.net.Uri

interface ServiceCommanderInterface {
    fun hasForInitialName(trackUri: Uri): Boolean
    fun startGPSLogging()
    fun stopGPSLogging()
    fun pauseGPSLogging()
    fun resumeGPSLogging()
}
