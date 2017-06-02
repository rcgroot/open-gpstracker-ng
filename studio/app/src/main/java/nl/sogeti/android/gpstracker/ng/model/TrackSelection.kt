package nl.sogeti.android.gpstracker.ng.model

import android.net.Uri

class TrackSelection {

    var trackUri: Uri? = null
        private set
    var trackName: String = ""
        private set

    private val listeners = mutableListOf<Listener>()

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun selectTrack(trackUri: Uri, trackName: String) {
        this.trackUri = trackUri
        this.trackName = trackName
        listeners.forEach {
            it.onTrackSelection(trackUri, this.trackName)
        }
    }

    interface Listener {
        fun onTrackSelection(trackUri: Uri, name: String)
    }

}
