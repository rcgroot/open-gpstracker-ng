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
    val contextWhenStarted: Context?
        get() = _context

    fun start(context: Context, navigation: NAV? = null) {
        if (_context == null) {
            _context = context
            _navigation = navigation
            didStart()
        } else {
            Timber.e("Starting already running presenter, ignoring call")
        }
    }

    fun stop() {
        if (_context != null) {
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
