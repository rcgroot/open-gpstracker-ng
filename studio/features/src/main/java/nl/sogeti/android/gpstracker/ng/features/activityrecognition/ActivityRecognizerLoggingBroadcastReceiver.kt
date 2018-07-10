package nl.sogeti.android.gpstracker.ng.features.activityrecognition

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateBroadcastReceiver
import javax.inject.Inject

class ActivityRecognizerLoggingBroadcastReceiver : LoggingStateBroadcastReceiver() {

    @Inject
    lateinit var activityRecognition: ActivityRecognition

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun didStopLogging(context: Context) {
        activityRecognition.stop()
    }

    override fun didPauseLogging(context: Context, trackUri: Uri) {
        activityRecognition.stop()
    }

    override fun didStartLogging(context: Context, trackUri: Uri) {
        activityRecognition.start(trackUri)
    }

    override fun onError(context: Context) {
        activityRecognition.stop()
    }

}
