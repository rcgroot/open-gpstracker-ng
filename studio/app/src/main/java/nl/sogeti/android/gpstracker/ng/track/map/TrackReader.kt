package nl.sogeti.android.gpstracker.ng.track.map

import android.content.Context
import android.net.Uri
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import nl.sogeti.android.gpstracker.ng.utils.DefaultResultHandler
import nl.sogeti.android.gpstracker.ng.utils.ResultHandler
import nl.sogeti.android.gpstracker.ng.utils.readTrack

class TrackReader(val context: Context, val trackUri: Uri, val action: (String, LatLngBounds, List<List<LatLng>>) -> Unit)
    : AsyncTask<Void, Void, ResultHandler>() {


    var isFinished = false
        private set

    override fun doInBackground(vararg p: Void): ResultHandler? {
        val handler = DefaultResultHandler()
        if (isCancelled) return null
        trackUri.readTrack(context, handler)
        if (isCancelled) return null
        val points = handler.waypoints.map { it.map { it.latLng } }
        val name = handler.name ?: ""
        action(name, handler.bounds, points)
        if (isCancelled) return null

        return handler
    }

    override fun onPostExecute(result: ResultHandler?) {
        super.onPostExecute(result)
        isFinished = true
    }

    override fun onCancelled() {
        super.onCancelled()
        isFinished = true
    }
}