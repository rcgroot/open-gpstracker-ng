package nl.sogeti.android.gpstracker.ng.features.activityrecognition

import android.app.IntentService
import android.content.Intent
import android.net.Uri
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.VALUE_TYPE_BIKE
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.VALUE_TYPE_CAR
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.VALUE_TYPE_DEFAULT
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.VALUE_TYPE_RUN
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.VALUE_TYPE_WALK
import nl.sogeti.android.gpstracker.ng.features.trackedit.saveTrackType

internal const val EXTRA_TRACK_URI = "EXTRA_TRACK_URI"

class ActivityRecognitionIntentService : IntentService("ActivityRecognitionIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        val result = ActivityTransitionResult.extractResult(intent)
        val trackUri = intent?.getParcelableExtra<Uri>(EXTRA_TRACK_URI)
        if (result != null && trackUri != null) {
            val trackType = when (result.transitionEvents.last().activityType) {
                DetectedActivity.WALKING -> TrackTypeDescriptions.trackTypeForContentType(VALUE_TYPE_WALK)
                DetectedActivity.RUNNING -> TrackTypeDescriptions.trackTypeForContentType(VALUE_TYPE_RUN)
                DetectedActivity.ON_BICYCLE -> TrackTypeDescriptions.trackTypeForContentType(VALUE_TYPE_BIKE)
                DetectedActivity.IN_VEHICLE -> TrackTypeDescriptions.trackTypeForContentType(VALUE_TYPE_CAR)
                else -> TrackTypeDescriptions.trackTypeForContentType(VALUE_TYPE_DEFAULT)
            }
            trackUri.saveTrackType(trackType)
        }
    }
}
