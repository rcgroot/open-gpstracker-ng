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

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.widget.Toast
import com.google.android.gms.common.api.GoogleApiClient
import nl.sogeti.android.gpstracker.service.integration.ContentConstants
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.CONTENT_URI
import timber.log.Timber
import java.util.*
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * Manager the exporting process
 */
internal class ExporterManager(private val context: Context) {
    private val listeners = HashSet<ProgressListener>()
    private val NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();
    private val executor = ThreadPoolExecutor(1, NUMBER_OF_CORES, 10, TimeUnit.SECONDS, LinkedBlockingDeque())
    private var shouldStop = false
    private val waypointProgressPerTrack = mutableMapOf<Uri, Int>()
    private val completedTracks = mutableSetOf<Uri>()
    private val progressListener = InnerProgressListener()

    fun startExport(driveApi: GoogleApiClient) {
        shouldStop = false
        val resolver = context.contentResolver
        var tracks: Cursor? = null
        var waypoints: Cursor? = null
        try {
            tracks = resolver.query(CONTENT_URI, arrayOf(ContentConstants.Tracks._ID), null, null, null)
            waypoints = resolver.query(ContentConstants.Waypoints.CONTENT_URI, arrayOf(Waypoints._ID), null, null, null)
            if (tracks?.moveToFirst() ?: false && waypoints?.moveToFirst() ?: false) {
                setTotalTrack(tracks.count)
                setTotalWaypoints(waypoints.count)
                do {
                    val id = tracks.getLong(0);
                    val trackUri = ContentUris.withAppendedId(CONTENT_URI, id);
                    waypointProgressPerTrack.put(trackUri, 0)
                    val creator = DriveUploadTask(context, trackUri, progressListener, driveApi)
                    creator.executeOn(executor)

                } while (tracks.moveToNext() && !shouldStop)
            } else {
                progressListener.showError("Finding tracks", context.getString(R.string.error_tracks_not_found), null);
            }
        } finally {
            tracks?.close()
            waypoints?.close()
        }
    }

    private fun setTotalWaypoints(count: Int) {
        listeners.forEach { it.updateExportProgress(totalWaypoints = count) }
    }

    private fun setTotalTrack(count: Int) {
        listeners.forEach { it.updateExportProgress(totalTracks = count) }
    }

    private fun updateProgress() {
        val completedTracks = completedTracks.size
        val completedWaypoints = waypointProgressPerTrack.values.sum()
        listeners.forEach {
            it.updateExportProgress(
                    isRunning = true,
                    completedTracks = completedTracks,
                    completedWaypoints = completedWaypoints)
        }
    }

    private fun finished() {
        val completedTracks = completedTracks.size
        val completedWaypoints = waypointProgressPerTrack.values.sum()
        listeners.forEach {
            it.updateExportProgress(
                    isRunning = false,
                    isFinished = true,
                    completedTracks = completedTracks,
                    completedWaypoints = completedWaypoints)
        }
    }

    fun stopExport() {
        shouldStop = true
        executor.queue.clear()
    }

    fun addListener(listener: ProgressListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ProgressListener) {
        listeners.remove(listener)
    }

    interface ProgressListener {

        fun updateExportProgress(isRunning: Boolean? = true, isFinished: Boolean? = false, completedTracks: Int? = null, totalTracks: Int? = null, completedWaypoints: Int? = null, totalWaypoints: Int? = null)
    }

    class InnerProgressListener : ProgressListener {
        override fun started(source: Uri?) {
            Timber.d("Started $source")
        }

        override fun setProgress(source: Uri?, value: Int) {
            if (source != null) {
                waypointProgressPerTrack.put(source, value)
            }
        }

        override fun finished(source: Uri?, result: Uri?) {
            Timber.d("Finished $source")
            if (source != null) {
                completedTracks.add(source)
                if (completedTracks.size == waypointProgressPerTrack.size) {
                    finished()
                } else {
                    updateProgress()
                }
            }
        }

        override fun showError(task: String?, errorMessage: String?, exception: Exception?) {
            Toast.makeText(context, "$task failed $errorMessage", Toast.LENGTH_SHORT).show()
        }
    }
}
