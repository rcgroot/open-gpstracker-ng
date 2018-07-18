package nl.sogeti.android.gpstracker.ng.features.util

import android.arch.lifecycle.ViewModel
import android.os.AsyncTask
import android.support.annotation.CallSuper
import android.support.annotation.WorkerThread

/**
 * Basis for a long lived (@see android.arch.lifecycle.ViewModel) Presenter that
 * can start / stop based on visibility to the user.
 *
 * Instantiate using the android.arch.lifecycle.ViewModel factory means.
 */
abstract class AbstractPresenter : ViewModel() {
    private var started = false
    private var dirty = true

    /**
     * Called when the Presenter is visible to the user. Will call onChange() on initial
     * start and when markDirty() was called when not started.
     */
    fun start() {
        started = true
        onStart()
        checkUpdate()
    }

    /**
     * Call when the Presenter is no longer started.
     */
    fun stop() {
        started = false
        onStop()
    }

    /**
     * Mark the Presenter as dirty when the update to the View would incur CPU, memory or other
     * load not useful when the presenter is started.
     */
    protected fun markDirty() {
        dirty = true
        checkUpdate()
    }

    /**
     * Override to start listening to model changes which incur load on the device. Such as
     * observing sensors or polling components.
     */
    @CallSuper
    open fun onStart() {
    }

    /**
     * Execute View updates deferred by using markDirty. Will run on a worker thread to allow
     * IO.
     */
    @WorkerThread
    abstract fun onChange()

    /**
     * Override to stop listening to model changes.
     */
    @CallSuper
    open fun onStop() {
    }

    private fun checkUpdate() {
        if (dirty && started) {
            dirty = false
            AsyncTask.THREAD_POOL_EXECUTOR.execute {
                onChange()
            }
        }
    }

}
