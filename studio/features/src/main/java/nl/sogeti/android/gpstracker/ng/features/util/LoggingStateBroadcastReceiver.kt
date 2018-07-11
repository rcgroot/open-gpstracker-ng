package nl.sogeti.android.gpstracker.ng.features.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

abstract class LoggingStateBroadcastReceiver : BroadcastReceiver() {

    @Inject
    @field:Named("stateBroadcastAction")
    lateinit var stateAction: String

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == stateAction) {
            onStateReceive(context, intent)
        }
    }

    private fun onStateReceive(context: Context, intent: Intent) {
        val state = intent.getIntExtra(ServiceConstants.EXTRA_LOGGING_STATE, ServiceConstants.STATE_UNKNOWN)
        if (intent.hasExtra(ServiceConstants.EXTRA_TRACK)) {
            val trackUri: Uri = intent.getParcelableExtra(ServiceConstants.EXTRA_TRACK)
            when (state) {
                ServiceConstants.STATE_LOGGING -> didStartLogging(context, trackUri)
                ServiceConstants.STATE_PAUSED -> didPauseLogging(context, trackUri)
                ServiceConstants.STATE_STOPPED -> didStopLogging(context)
            }
        } else {
            Timber.e("Failed to handle state change $intent")
            onError(context)
        }
    }

    abstract fun didStopLogging(context: Context)

    abstract fun didPauseLogging(context: Context, trackUri: Uri)

    abstract fun didStartLogging(context: Context, trackUri: Uri)

    abstract fun onError(context: Context)
}
