package nl.sogeti.android.gpstracker.ng.features.util

import android.arch.lifecycle.Observer
import android.net.Uri
import android.support.annotation.CallSuper
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.service.util.readName

abstract class AbstractSelectedTrackPresenter : AbstractTrackPresenter() {

    protected val trackSelection = FeatureConfiguration.featureComponent.trackSelection()
    private val selectionObserver = Observer<Uri> { trackUri -> onTrackSelected(trackUri) }

    init {
        trackSelection.selection.observeForever(selectionObserver)
        val selectedTrack = trackSelection.selection.value
        selectedTrack?.let {
            onTrackSelected(selectedTrack)
        }
    }

    override fun onStart() {
        super.onStart()
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
