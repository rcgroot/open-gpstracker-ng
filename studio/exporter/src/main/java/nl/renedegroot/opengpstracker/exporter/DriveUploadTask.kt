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
import android.net.Uri
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Result
import com.google.android.gms.drive.*
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.Query
import com.google.android.gms.drive.query.SearchableField
import nl.renedegroot.opengpstracker.exporter.gpx.GpxCreator
import timber.log.Timber
import java.util.concurrent.ThreadPoolExecutor

private const val FOLDER_NAME = "Open GPS Tracker - Exports"
private const val FOLDER_MIME = "application/vnd.google-apps.folder"

/**
 * Async task that uses the GpxCreate URI to Stream capability to fill a Google Drive file with said stream
 */
internal class DriveUploadTask(
        cntentResolver: ContentResolver,
        val trackUri: Uri,
        private val callback: DriveUploadTask.Callback,
        private val driveApi: GoogleApiClient) {

    private val gpxCreator = GpxCreator(cntentResolver, trackUri)
    private var isCancelled: Boolean = false
    private val filename: String by lazy { GpxCreator.fileName(trackUri) }


    fun executeOn(executor: ThreadPoolExecutor) {
        executor.execute {
            doInBackground()
            if (!isCancelled) {
                callback.onFinished(trackUri)
            }
        }
    }

    fun cancel() {
        isCancelled = true
        callback.onCancel()

    }

    private fun doInBackground() {
        Timber.d("Looking for export folder")
        val rootFolder = Drive.DriveApi.getRootFolder(driveApi);
        val query = Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, FOLDER_NAME))
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, FOLDER_MIME))
                .build()
        val rootListResult = rootFolder?.queryChildren(driveApi, query)?.await()
        processResult(rootListResult)
        Timber.d("Searched for export folder $rootListResult")
        if (isCancelled) {
            return
        }

        val exportFolder: DriveFolder
        val gpxListResult: DriveApi.MetadataBufferResult?
        if (rootListResult?.metadataBuffer?.count ?: 0 > 0) {
            Timber.d("Found export folder")
            val folderId = rootListResult?.metadataBuffer?.get(0)?.driveId
            exportFolder = folderId?.asDriveFolder()!!
            Timber.d("Have export folder $exportFolder")

            Timber.d("Looking for GPX file")
            val fileQuery = Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, filename))
                    .addFilter(Filters.eq(SearchableField.MIME_TYPE, GpxCreator.MIME_TYPE_GPX))
                    .build()
            gpxListResult = exportFolder.queryChildren(driveApi, fileQuery).await()
            processResult(gpxListResult)
            Timber.d("Searched for GPX file $gpxListResult")
            if (isCancelled) {
                return
            }
        } else {
            gpxListResult = null
            Timber.d("Creating export folder")
            val metadata = MetadataChangeSet.Builder()
                    .setTitle(FOLDER_NAME)
                    .build();
            val createFolderResult = rootFolder?.createFolder(driveApi, metadata)?.await()
            processResult(createFolderResult)
            if (isCancelled) {
                return
            }
            exportFolder = createFolderResult!!.driveFolder
            Timber.d("Created export folder $exportFolder")
        }
        rootListResult?.metadataBuffer?.release()


        if (gpxListResult == null || gpxListResult.metadataBuffer.count == 0) {
            Timber.d("Creating GPX content")
            val driveContentsResult = Drive.DriveApi.newDriveContents(driveApi).await();
            processResult(driveContentsResult)
            if (isCancelled) {
                return
            }
//            super.exportGpx(driveContentsResult.driveContents.outputStream)
            gpxCreator.createGpx(driveContentsResult.driveContents.outputStream)
            if (isCancelled) {
                return
            }
            Timber.d("Created GPX content $driveContentsResult")
            Timber.d("Creating GPX file")
            val metadata = MetadataChangeSet.Builder()
                    .setTitle(filename)
                    .setMimeType(GpxCreator.MIME_TYPE_GPX)
                    .build()
            val fileResult = exportFolder.createFile(driveApi, metadata, driveContentsResult.driveContents).await();
            processResult(fileResult)
            if (isCancelled) {
                return
            }
            Timber.d("Creating GPX file $fileResult")
        } else {
            Timber.d("Found gpx file $gpxListResult")
            val fileId = gpxListResult.metadataBuffer.get(0).driveId
            val exportFile = fileId.asDriveFile()
            val openFile = exportFile.open(driveApi, DriveFile.MODE_WRITE_ONLY, null).await()
            gpxListResult.metadataBuffer.release()
            Timber.d("Have gpx file $openFile")
            Timber.d("Overwrite GPX content")
//            super.exportGpx(openFile.driveContents.outputStream)
            gpxCreator.createGpx(openFile.driveContents.outputStream)
            val writeResult = openFile.driveContents.commit(driveApi, null).await()
            processResult(writeResult)
            if (isCancelled) {
                return
            }
            Timber.d("Overwriten GPX content $writeResult")
        }

        return
    }

    internal fun processResult(result: Result?): Boolean {
        val isSuccess = result != null && result.status.isSuccess
        if (!isSuccess) {
            isCancelled = true
            callback.onError(result?.status?.statusMessage ?: "Missing error description")
        }

        return isSuccess
    }

    interface Callback {
        fun onError(message: String)
        fun onCancel()
        fun onFinished(trackUri: Uri)
    }
}
