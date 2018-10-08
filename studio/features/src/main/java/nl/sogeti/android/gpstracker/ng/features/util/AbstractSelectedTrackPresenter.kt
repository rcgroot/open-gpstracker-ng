package nl.sogeti.android.gpstracker.ng.features.util

import androidx.lifecycle.Observer
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.service.util.readName

abstract class AbstractSelectedTrackPresenter : AbstractTrackPresenter() {

    protected val trackSelection = FeatureConfiguration.featureComponent.trackSelection()
    private val selectionObserver = Observer<Uri> { trackUri -> onTrackSelected(trackUri) }

    override fun onStart() {
        super.onStart()
        trackSelection.selection.observeForever(selectionObserver)
    }

    override fun onStop() {
        trackSelection.selection.removeObserver(selectionObserver)
        super.onStop()
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
