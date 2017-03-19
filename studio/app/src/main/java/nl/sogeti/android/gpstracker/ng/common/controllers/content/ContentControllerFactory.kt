package nl.sogeti.android.gpstracker.ng.common.controllers.content

import android.content.Context


class ContentControllerFactory {

    fun createContentController(context: Context, listener: ContentController.Listener): ContentController {
        return ContentController(context, listener);
    }
}