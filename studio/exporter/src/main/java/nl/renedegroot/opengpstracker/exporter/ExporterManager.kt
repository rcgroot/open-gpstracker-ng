/*
 * Open GPS Tracker
 * Copyright (C) 2018  René de Groot
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package nl.renedegroot.opengpstracker.exporter

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.common.api.GoogleApiClient
import nl.sogeti.android.gpstracker.service.integration.ContentConstants
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.CONTENT_URI
import nl.sogeti.android.gpstracker.service.util.waypointsUri
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Manager the exporting process
 */
internal class ExporterManager(private val contentResolver: ContentResolver) : DriveUploadTask.Callback {

    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private val executor = ThreadPoolExecutor(1, NUMBER_OF_CORES, 10, TimeUnit.SECONDS, LinkedBlockingDeque())
    private var shouldStop = false
    private val waypointProgressPerTrack = mutableMapOf<Uri, Int>()
    private val completedTracks = mutableSetOf<Uri>()

    val state = MutableLiveData<ExportState>()

    init {
        state.postValue(ExportState.Idle)
    }

    sealed class ExportState {
        object Idle : ExportState()
        class Active(val completedTracks: Int, val totalTracks: Int, val completedWaypoints: Int, val totalWaypoints: Int) : ExportState()
        class Finished(val completedTracks: Int, val completedWaypoints: Int) : ExportState()
        class Error(@StringRes val message: Int) : ExportState()
    }

    fun startExport(driveApi: GoogleApiClient) {
        shouldStop = false
        var tracks: Cursor? = null
        var waypoints: Cursor? = null
        try {
            tracks = contentResolver.query(CONTENT_URI, arrayOf(ContentConstants.Tracks._ID), null, null, null)
            waypoints = contentResolver.query(waypointsUri(), arrayOf(ContentConstants.Waypoints._ID), null, null, null)
            if (tracks?.moveToFirst() == true && waypoints?.moveToFirst() == true) {
                state.postValue(ExportState.Active(0, tracks.count, 0, waypoints.count))
                do {
                    val id = tracks.getLong(0);
                    val trackUri = ContentUris.withAppendedId(CONTENT_URI, id);
                    waypointProgressPerTrack[trackUri] = 0
                    val creator = DriveUploadTask(contentResolver, trackUri, this, driveApi)
                    creator.executeOn(executor)

                } while (tracks.moveToNext() && !shouldStop)
            } else {
                state.postValue(ExportState.Error(R.string.exporter__error_tracks_not_found))
            }
        } finally {
            tracks?.close()
            waypoints?.close()
        }
    }

    fun stopExport() {
        shouldStop = true
        executor.queue.clear()
    }

    override fun onError(message: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onCancel() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onFinished(trackUri: Uri) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    private fun updateProgress(active: ExportState.Active) {
        val completedTracks = completedTracks.size
        val completedWaypoints = waypointProgressPerTrack.values.sum()
        state.postValue(ExportState.Active(completedTracks, active.totalTracks, completedWaypoints, active.totalWaypoints))
    }

    private fun finished() {
        val completedTracks = completedTracks.size
        val completedWaypoints = waypointProgressPerTrack.values.sum()
        state.postValue(ExportState.Finished(completedTracks, completedWaypoints))
    }
}
