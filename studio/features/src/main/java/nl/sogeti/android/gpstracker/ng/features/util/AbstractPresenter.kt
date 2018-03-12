package nl.sogeti.android.gpstracker.ng.features.util

import android.arch.lifecycle.ViewModel
import android.support.annotation.CallSuper

abstract class AbstractPresenter : ViewModel() {
    private var started = false
    private var dirty = true

    fun start() {
        started = true
        onStart()
        checkUpdate()
    }

    fun stop() {
        started = false
        onStop()
    }

    private fun checkUpdate() {
        if (dirty && started) {
            dirty = false
            onChange()
        }
    }

    protected fun markDirty() {
        dirty = true
        checkUpdate()
    }

    @CallSuper
    open fun onStart() {
    }

    abstract fun onChange()

    @CallSuper
    open fun onStop() {
    }
}
