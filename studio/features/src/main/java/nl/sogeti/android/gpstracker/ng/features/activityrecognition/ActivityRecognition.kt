package nl.sogeti.android.gpstracker.ng.features.activityrecognition

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import timber.log.Timber
import javax.inject.Inject

class ActivityRecognition @Inject constructor(
        private val context: Context,
        private val client: ActivityRecognitionClient) {

    private var pendingIntent: PendingIntent? = null

    private fun createPendingIntent(trackUri: Uri): PendingIntent? {
        val intent = Intent(context, ActivityRecognitionIntentService::class.java)
        intent.putExtra(EXTRA_TRACK_URI, trackUri)
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    fun start(trackUri: Uri) {
        stop()
        pendingIntent = createPendingIntent(trackUri)
        val task = client.requestActivityTransitionUpdates(createRequest(), pendingIntent)!!
        task.addOnSuccessListener { Timber.i("Successfully request activity transition updates") }
        task.addOnFailureListener { exception ->  Timber.e("Failed to request activity transition updates. Error status $exception") }
    }

    private fun createRequest(): ActivityTransitionRequest {
        val transitions = listOf(
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.WALKING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build(),
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.RUNNING)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build(),
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.ON_BICYCLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build(),
                ActivityTransition.Builder()
                        .setActivityType(DetectedActivity.IN_VEHICLE)
                        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                        .build()
                )

        return ActivityTransitionRequest(transitions);
    }

    fun stop() {
        client.removeActivityUpdates(pendingIntent)
    }

}
