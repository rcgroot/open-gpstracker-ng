package nl.sogeti.android.gpstracker.ng.common.controllers.content

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper

/**
 * Control the observing and monitoring for a observable uri content.
 */
class ContentController(private val context: Context, private val listener: Listener) {

    private val contentObserver = ContentObserver()

    fun registerObserver(toUri: Uri?) {
        contentObserver.register(toUri)
    }

    fun unregisterObserver() {
        contentObserver.unregister()
    }

    private inner class ContentObserver : android.database.ContentObserver(Handler(Looper.getMainLooper())) {

        private var registeredUri: Uri? = null

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
                registeredUri = null
            }
        }

        override fun onChange(selfChange: Boolean, changedUri: Uri) {
            registeredUri?.let { listener.onChangeUriContent(it, changedUri) }

        }
    }

    interface Listener {
        fun onChangeUriContent(contentUri: Uri, changesUri: Uri)
    }
}