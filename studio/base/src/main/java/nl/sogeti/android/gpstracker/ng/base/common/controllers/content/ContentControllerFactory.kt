package nl.sogeti.android.gpstracker.ng.base.common.controllers.content

import android.content.Context
import javax.inject.Inject


class ContentControllerFactory @Inject constructor(private val context: Context) {

    fun createContentController(listener: ContentController.Listener): ContentController {
        return ContentController(context, listener)
    }
}
