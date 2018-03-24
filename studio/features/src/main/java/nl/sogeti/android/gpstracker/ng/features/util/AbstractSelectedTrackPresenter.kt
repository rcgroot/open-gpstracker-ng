package nl.sogeti.android.gpstracker.ng.features.util

import android.arch.lifecycle.Observer
import android.net.Uri
import android.support.annotation.CallSuper
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.features.model.TrackSelection
import nl.sogeti.android.gpstracker.service.util.readName

abstract class AbstractSelectedTrackPresenter(
        private val trackSelection: TrackSelection,
        contentController: ContentController) : AbstractTrackPresenter(contentController) {

    private val selectionObserver = Observer<Uri> { trackUri -> onTrackSelected(trackUri) }

    init {
        trackSelection.selection.observeForever(selectionObserver)
        val selectedTrack = trackSelection.selection.value
        selectedTrack?.let {
            onTrackSelected(selectedTrack)
        }
    }

    @CallSuper
    public
    override fun onCleared() {
        trackSelection.selection.removeObserver(selectionObserver)
        super.onCleared()
    }

    private fun onTrackSelected(trackUri: Uri?) {
        super.trackUri = trackUri
    }

    final override fun onChange() {
        val trackName = trackUri?.readName() ?: ""
        onTrackUpdate(trackUri, trackName)
    }

    abstract fun onTrackUpdate(trackUri: Uri?, name: String)
}
