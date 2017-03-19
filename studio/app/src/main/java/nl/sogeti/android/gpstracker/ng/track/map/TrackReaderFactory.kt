package nl.sogeti.android.gpstracker.ng.track.map

import android.content.Context
import android.net.Uri

class TrackReaderFactory {

    fun createTrackReader(context: Context, trackUri: Uri, viewModel: TrackMapViewModel): TrackReader {
        return TrackReader(context, trackUri, viewModel)
    }
}