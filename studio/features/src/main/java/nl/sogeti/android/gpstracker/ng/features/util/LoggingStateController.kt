package nl.sogeti.android.gpstracker.ng.features.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.base.common.onMainThread
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants
import nl.sogeti.android.gpstracker.service.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.service.util.trackUri
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class LoggingStateController @Inject constructor(
        private val context: Context,
        private val serviceManager: ServiceManagerInterface,
        @Named("stateBroadcastAction")
        private var stateBroadcastAction: String) {

    private var loggingStateReceiver: BroadcastReceiver? = null
    private var listener: LoggingStateListener? = null

    internal var loggingState = ServiceConstants.STATE_UNKNOWN
        private set
    internal var trackUri: Uri? = null
        private set

    fun connect(connectListener: LoggingStateListener? = null) {
        this.listener = connectListener
        registerReceiver()
        serviceManager.startup {
            synchronized(this) {
                onMainThread {
                    val trackId = serviceManager.trackId
                    if (trackId > 0) {
                        trackUri = trackUri(trackId)
                    }
                    loggingState = serviceManager.loggingState
                    Timber.d("onConnect LoggerState %s %d", trackUri, loggingState)
                    listener?.didConnectToService(context, loggingState, trackUri)
                }
            }
        }
    }

    fun disconnect() {
        unregisterReceiver()
        serviceManager.shutdown()
        listener = null
    }

    private fun registerReceiver() {
        unregisterReceiver()
        loggingStateReceiver = LoggerStateReceiver()
        context.registerReceiver(loggingStateReceiver, IntentFilter(stateBroadcastAction))
    }

    private fun unregisterReceiver() {
        if (loggingStateReceiver != null) {
            context.unregisterReceiver(loggingStateReceiver)
            loggingStateReceiver = null
        }
    }

    private inner class LoggerStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            loggingState = intent.getIntExtra(ServiceConstants.EXTRA_LOGGING_STATE, ServiceConstants.STATE_UNKNOWN)
            trackUri = intent.getParcelableExtra(ServiceConstants.EXTRA_TRACK)
            val name = intent.getStringExtra(ServiceConstants.EXTRA_TRACK_NAME)

            Timber.d("onReceive LoggerStateReceiver %s %s %d", trackUri, name, loggingState)
            listener?.didChangeLoggingState(context, loggingState, trackUri)
        }
    }
}

interface LoggingStateListener {

    fun didChangeLoggingState(context: Context, loggingState: Int, trackUri: Uri?)

    fun didConnectToService(context: Context, loggingState: Int, trackUri: Uri?)

}
