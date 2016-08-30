package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.Context
import timber.log.Timber


abstract class ContextedPresenter {
    var context: Context? = null

    fun start(context: Context) {
        if (this.context == null) {
            this.context = context
            didStart()
        } else {
            Timber.e("Starting already running presenter, ignoring call")
        }
    }

    fun stop() {
        if (context != null) {
            willStop()
            context = null
        } else {
            Timber.e("Stopping not running running presenter, ignoring call")
        }
    }

    abstract fun didStart()

    abstract fun willStop()

}