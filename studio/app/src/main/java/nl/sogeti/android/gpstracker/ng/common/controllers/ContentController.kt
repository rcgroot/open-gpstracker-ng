package nl.sogeti.android.gpstracker.ng.common.controllers

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper

/**
 * Control the observing and monitoring for a observable uri value
 * and its content.
 */
class ContentController(val context: Context, val listener: ContentListener) {

    private val contentObserver = ContentObserver()

    fun registerObserver(toUri: Uri?) {
        contentObserver.register(toUri)
    }

    fun unregisterObserver() {
        contentObserver.unregister()
    }

    private inner class ContentObserver : android.database.ContentObserver(Handler(Looper.getMainLooper())) {

        var registeredUri: Uri? = null

        fun register(uri: Uri?) {
            unregister()
            registeredUri = uri
            if (uri != null) {
                context.contentResolver.registerContentObserver(uri, true, this)
            }
        }

        fun unregister() {
            if (registeredUri != null) {
                context.contentResolver.unregisterContentObserver(contentObserver)
                registeredUri = null4§
            }
        }

        override fun onChange(selfChange: Boolean, changedUri: Uri) {
            registeredUri?.let { listener.onChangeUriContent(it, changedUri) }

        }
    }

    interface ContentListener {
        fun onChangeUriContent(contentUri: Uri, changesUri: Uri)
    }
}