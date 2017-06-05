package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.Context
import timber.log.Timber


abstract class ContextedPresenter {
    private var _context: Context? = null
    val context: Context
    get() {
        return _context ?: throw IllegalStateException("Don't run the presenter outside its started state")
    }
    val isStarted: Boolean = _context != null


    fun start(context: Context) {
        if (this._context == null) {
            this._context = context
            didStart()
        } else {
            Timber.e("Starting already running presenter, ignoring call")
        }
    }

    fun stop() {
        if (_context != null) {
            willStop()
            _context = null
        } else {
            Timber.e("Stopping not running running presenter, ignoring call")
        }
    }

    abstract fun didStart()

    abstract fun willStop()

}
