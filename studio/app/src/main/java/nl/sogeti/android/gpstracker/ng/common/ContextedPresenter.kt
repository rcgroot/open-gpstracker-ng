package nl.sogeti.android.gpstracker.ng.common

import android.content.Context
import timber.log.Timber


abstract class ContextedPresenter {
    private var privateContext: Context? = null
    val context: Context?
        get() = privateContext

    fun start(context: Context) {
        if (privateContext == null) {
            this.privateContext = context
            didStart()
        } else {
            Timber.e("Starting already running presenter, ignoring call")
        }
    }

    fun stop() {
        if (privateContext != null) {
            willStop()
            privateContext = null
        } else {
            Timber.e("Stopping not running running presenter, ignoring call")
        }
    }

    abstract fun didStart()

    abstract fun willStop()

}