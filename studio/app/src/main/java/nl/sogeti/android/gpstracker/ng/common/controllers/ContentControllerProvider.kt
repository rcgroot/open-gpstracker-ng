package nl.sogeti.android.gpstracker.ng.common.controllers

import android.content.Context


class ContentControllerProvider {

    fun createContentControllerProvider(context: Context, listener: ContentController.ContentListener): ContentController {
        return ContentController(context, listener);
    }
}