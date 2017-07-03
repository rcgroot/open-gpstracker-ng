package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.Context
import timber.log.Timber


abstract class ContextedPresenter<NAV: Navigation> {

    private var _navigation: NAV? = null
    val navigation: NAV
        get() {
            return _navigation ?: throw IllegalStateException("Don't run the presenter outside its started state")
        }
    private var _context: Context? = null
    val context: Context
        get() {
            return _context ?: throw IllegalStateException("Don't run the presenter outside its started state")
        }
    val isStarted: Boolean
        get() = _context != null

    fun start(context: Context, navigation: NAV? = null) {
        if (!isStarted) {
            _navigation = navigation
            _context = context
            didStart()
        } else {
            Timber.e("Starting already running presenter, ignoring call")
        }
    }

    fun stop() {
        if (isStarted) {
            willStop()
            _context = null
            _navigation = null
        } else {
            Timber.e("Stopping not running running presenter, ignoring call")
        }
    }

    abstract fun didStart()

    abstract fun willStop()

}
