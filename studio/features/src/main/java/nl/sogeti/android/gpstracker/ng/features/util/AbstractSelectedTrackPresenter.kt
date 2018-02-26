package nl.sogeti.android.gpstracker.ng.features.util

import android.net.Uri
import android.support.annotation.CallSuper
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.service.util.trackUri
import javax.inject.Inject

abstract class AbstractSelectedTrackPresenter : AbstractTrackPresenter(trackUri(ContentController.NO_CONTENT_ID)), TrackSelection.Listener {

    @Inject
    lateinit var trackSelection: TrackSelection

    private lateinit var trackName: String

    init {
        FeatureConfiguration.featureComponent.inject(this)
        trackSelection.addListener(this)
        val selectedTrack = trackSelection.trackUri
        selectedTrack?.let {
            onTrackSelection(selectedTrack, trackSelection.trackName)
        }
    }

    @CallSuper
    override fun onCleared() {
        trackSelection.removeListener(this)
        super.onCleared()
    }

    final override fun onTrackSelection(trackUri: Uri, trackName: String) {
        this.trackName = trackName
        super.trackUri = trackUri
    }

    final override fun onChange() {
        onTrackUpdate(trackUri, trackName)
    }

    abstract fun onTrackUpdate(trackUri: Uri, name: String)
}
