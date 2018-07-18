package nl.sogeti.android.gpstracker.ng.features.util

import android.net.Uri
import android.support.annotation.CallSuper
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import javax.inject.Inject

abstract class AbstractTrackPresenter : AbstractPresenter(), ContentController.Listener {

    @Inject
    lateinit var contentController: ContentController

    var trackUri: Uri? = null
        set(value) {
            field = value
            contentController.registerObserver(this, trackUri)
            markDirty()
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
