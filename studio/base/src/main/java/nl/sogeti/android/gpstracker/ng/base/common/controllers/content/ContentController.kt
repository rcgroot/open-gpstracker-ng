package nl.sogeti.android.gpstracker.ng.base.common.controllers.content

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import javax.inject.Inject

/**
 * Control the observing and monitoring for a observable uri content.
 */
class ContentController @Inject constructor(private val context: Context) {

    private var listener: Listener? = null

    private val contentObserver = ContentObserver()

    fun registerObserver(listener: Listener, toUri: Uri?) {
        contentObserver.unregister()
        this.listener = listener
        contentObserver.register(toUri)
    }

    fun unregisterObserver() {
        contentObserver.unregister()
        this.listener = null
    }

    private inner class ContentObserver : android.database.ContentObserver(Handler(Looper.getMainLooper())) {

        private var registeredUri: Uri? = null

        fun register(uri: Uri?) {
            unregister()
            registeredUri = uri
            if (uri != null && uri.lastPathSegment != NO_CONTENT_ID.toString()) {
                context.contentResolver.registerContentObserver(uri, true, this)
            }
        }

        fun unregister() {
            val uri = registeredUri
            if (uri != null && uri.lastPathSegment != NO_CONTENT_ID.toString()) {
                context.contentResolver.unregisterContentObserver(contentObserver)
                registeredUri = null
            }
        }

        override fun onChange(selfChange: Boolean, changedUri: Uri) {
            registeredUri?.let { listener?.onChangeUriContent(it, changedUri) }

        }
    }

    interface Listener {
        fun onChangeUriContent(contentUri: Uri, changesUri: Uri)
    }

    companion object {
        const val NO_CONTENT_ID = -1L
    }
}
