package nl.sogeti.android.gpstracker.ng.sharing

import android.content.Intent
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.utils.sharedTrackUri

class ShareIntentFactory {

    fun createShareIntent(track: Uri): Intent {
        val shareIntent = Intent()
        val trackStream = sharedTrackUri(track.lastPathSegment.toLong())
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, trackStream)
        shareIntent.type = GpxShareProvider.TRACK_MIME_TYPE

        return shareIntent
    }
}