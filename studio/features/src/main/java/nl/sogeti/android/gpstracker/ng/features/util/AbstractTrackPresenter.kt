package nl.sogeti.android.gpstracker.ng.features.util

import android.net.Uri
import android.support.annotation.CallSuper
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController

abstract class AbstractTrackPresenter(private val contentController: ContentController) : AbstractPresenter(), ContentController.Listener {

    var trackUri : Uri? = null
        set(value) {
            field = value
            contentController.registerObserver(trackUri)
            markDirty()
        }

    init {
        this.contentController.listener = this
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
