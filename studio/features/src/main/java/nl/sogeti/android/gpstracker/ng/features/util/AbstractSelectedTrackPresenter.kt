package nl.sogeti.android.gpstracker.ng.features.util

import android.net.Uri
import android.support.annotation.CallSuper
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.features.model.TrackSelection
import nl.sogeti.android.gpstracker.service.util.readName

abstract class AbstractSelectedTrackPresenter(val trackSelection: TrackSelection, contentController: ContentController) : AbstractTrackPresenter(contentController), TrackSelection.Listener {

    private var trackName = ""

    init {
        trackSelection.addListener(this)
        val selectedTrack = trackSelection.trackUri
        selectedTrack?.let {
            onTrackSelection(selectedTrack, trackSelection.trackName)
        }
    }

    @CallSuper
    public
    override fun onCleared() {
        trackSelection.removeListener(this)
        super.onCleared()
    }

    final override fun onTrackSelection(trackUri: Uri, trackName: String) {
        this.trackName = trackName
        super.trackUri = trackUri
    }

    final override fun onChange() {
        trackName = trackUri?.readName() ?: ""
        onTrackUpdate(trackUri, trackName)
    }

    abstract fun onTrackUpdate(trackUri: Uri?, name: String)
}
