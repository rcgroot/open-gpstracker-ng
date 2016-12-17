package nl.sogeti.android.gpstracker.ng.common.controllers

import android.content.Context
import android.databinding.Observable
import android.databinding.ObservableField
import android.net.Uri

/**
 * Control the observing and monitoring for a observable uri value
 * and its content.
 */
class ContentController(val context: Context, field: ObservableField<Uri>, val listener: ContentListener) {

    private val fieldObserver = FieldObserver(field)
    private var contentObserver = ContentObserver(field.get())

    private fun switchContentObserver(toUri: Uri) {
        contentObserver.unregister()
        contentObserver = ContentObserver(toUri)
        listener.onChangeUriContent(toUri, toUri)
    }

    fun destroy() {
        fieldObserver.unregister()
        contentObserver.unregister()
    }

    private inner class FieldObserver(val field: ObservableField<Uri>) : Observable.OnPropertyChangedCallback() {

        init {
            field.addOnPropertyChangedCallback(this)
        }

        fun unregister() {
            field.removeOnPropertyChangedCallback(this)
        }

        override fun onPropertyChanged(sender: Observable, propertyId: Int) {
            val newUri = field.get()
            listener.onChangeUriField(newUri)
            switchContentObserver(newUri)
        }
    }

    private inner class ContentObserver(val uri: Uri) : android.database.ContentObserver(null) {
        init {
            context.contentResolver.registerContentObserver(uri, true, this)
        }

        fun unregister() {
            context.contentResolver.unregisterContentObserver(contentObserver)
        }

        override fun onChange(selfChange: Boolean, uri: Uri) {
            listener.onChangeUriContent(this.uri, uri)
        }
    }

    interface ContentListener {
        fun onChangeUriField(uri: Uri)
        fun onChangeUriContent(contentUri: Uri, changesUri: Uri)
    }
}