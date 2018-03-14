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
import nl.sogeti.android.gpstracker.utils.onMainThread
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
    internal var lastState = ServiceConstants.STATE_UNKNOWN
        private set

    fun connect(listener: LoggingStateListener? = null) {
        registerReceiver()
        serviceManager.startup(context) {
            synchronized(this) {
                onMainThread {
                    val trackId = serviceManager.trackId
                    var uri: Uri? = null
                    var name: String? = null
                    if (trackId > 0) {
                        uri = trackUri(trackId)
                        name = uri.runQuery(BaseConfiguration.appComponent.contentResolver()) { cursor -> cursor.getString(NAME) }
                    }
                    lastState = serviceManager.loggingState
                    Timber.d("onConnect LoggerState %s %s %d", uri, name, lastState)
                    listener?.didConnectToService(context, uri, name, lastState)
                }
            }
        }
    }

    fun disconnect() {
        unregisterReceiver()
        serviceManager.shutdown(context)
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
            lastState = intent.getIntExtra(ServiceConstants.EXTRA_LOGGING_STATE, ServiceConstants.STATE_UNKNOWN)
            val trackUri = intent.getParcelableExtra<Uri>(ServiceConstants.EXTRA_TRACK)
            val name = intent.getStringExtra(ServiceConstants.EXTRA_TRACK_NAME)

            Timber.d("onReceive LoggerStateReceiver %s %s %d", trackUri, name, lastState)
            listener?.didChangeLoggingState(context, trackUri, name, lastState)
        }
    }
}

interface LoggingStateListener {

    fun didChangeLoggingState(context: Context, trackUri: Uri?, name: String?, loggingState: Int)

    fun didConnectToService(context: Context, trackUri: Uri?, name: String?, loggingState: Int)

}
