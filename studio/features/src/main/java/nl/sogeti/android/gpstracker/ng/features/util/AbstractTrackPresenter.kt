package nl.sogeti.android.gpstracker.ng.features.util

import android.net.Uri
import android.support.annotation.CallSuper
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import javax.inject.Inject

abstract class AbstractTrackPresenter(trackUri: Uri) : AbstractPresenter(), ContentController.Listener {

    @Inject
    lateinit var contentControllerFactory: ContentControllerFactory

    private val contentController: ContentController

    var trackUri = trackUri
        set(value) {
            field = value
            contentController.registerObserver(trackUri)
            markDirty()
        }

    init {
        FeatureConfiguration.featureComponent.inject(this)
        contentController = contentControllerFactory.createContentController(this)
        this.trackUri = trackUri
    }

    @CallSuper
    override fun onCleared() {
        contentController.unregisterObserver()
        super.onCleared()
    }

    final override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        markDirty()
    }
}
