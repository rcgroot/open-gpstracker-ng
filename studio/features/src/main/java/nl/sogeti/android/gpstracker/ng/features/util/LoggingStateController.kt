package nl.sogeti.android.gpstracker.ng.features.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.TracksColumns.NAME
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants
import nl.sogeti.android.gpstracker.service.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.service.util.trackUri
import nl.sogeti.android.gpstracker.utils.contentprovider.getString
import nl.sogeti.android.gpstracker.utils.contentprovider.runQuery
import nl.sogeti.android.gpstracker.ng.base.common.onMainThread
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class LoggingStateController @Inject constructor(
        private val context: Context,
        private val serviceManager: ServiceManagerInterface,
        @Named("stateBroadcastAction")
        private var stateBroadcastAction: String) {

    private var loggingStateReceiver: BroadcastReceiver? = null

    var listener: LoggingStateListener? = null

    internal var loggingState = ServiceConstants.STATE_UNKNOWN
        private set
    internal var trackUri : Uri? = null
        private set

    fun connect(listener: LoggingStateListener? = null) {
        registerReceiver()
        serviceManager.startup {
            synchronized(this) {
                onMainThread {
                    val trackId = serviceManager.trackId
                    var name: String? = null
                    if (trackId > 0) {
                        trackUri = trackUri(trackId)
                        name = trackUri?.runQuery(BaseConfiguration.appComponent.contentResolver()) { cursor -> cursor.getString(NAME) }
                    }
                    loggingState = serviceManager.loggingState
                    Timber.d("onConnect LoggerState %s %s %d", trackUri, name, loggingState)
                    listener?.didConnectToService(context, trackUri, name, loggingState)
                }
            }
        }
    }

    fun disconnect() {
        unregisterReceiver()
        serviceManager.shutdown()
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
            listener?.didChangeLoggingState(context, trackUri, name, loggingState)
        }
    }
}

interface LoggingStateListener {

    fun didChangeLoggingState(context: Context, trackUri: Uri?, name: String?, loggingState: Int)

    fun didConnectToService(context: Context, trackUri: Uri?, name: String?, loggingState: Int)

}
