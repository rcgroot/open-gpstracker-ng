package nl.sogeti.android.gpstracker.ng.track.map

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import nl.sogeti.android.gpstracker.ng.utils.DefaultResultHandler
import nl.sogeti.android.gpstracker.ng.utils.readTrack

class TrackReader(val context: Context, val trackUri: Uri, private val viewModel: TrackMapViewModel)
    : AsyncTask<Void, Void, Void>() {

    private val handler = DefaultResultHandler()
    var isFinished = false
        private set

    override fun doInBackground(vararg p: Void): Void? {
        if (isCancelled) return null
        trackUri.readTrack(context, handler, null)
        if (isCancelled) return null
        updateViewModelWithHandler(handler)
        if (isCancelled) return null

        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        isFinished = false
    }

    override fun onCancelled() {
        super.onCancelled()
        isFinished = false
    }

    fun updateViewModelWithHandler(handler: DefaultResultHandler) {
        handler.headBuilder?.let { viewModel.trackHeadBounds.set(it.build()) }
        handler.boundsBuilder?.let { viewModel.completeBounds.set(it.build()) }
        viewModel.name.set(handler.name)
        val points = handler.waypoints.map { it.map { it.latLng } }
        val filteredPoints = points.filter { it.count() > 1 }
        viewModel.waypoints.set(filteredPoints)
    }
}