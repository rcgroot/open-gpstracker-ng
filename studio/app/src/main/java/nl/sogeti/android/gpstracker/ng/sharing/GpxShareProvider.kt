package nl.sogeti.android.gpstracker.ng.sharing

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import nl.sogeti.android.gpstracker.ng.sharing.tasks.GpxCreator
import nl.sogeti.android.gpstracker.ng.utils.trackUri
import nl.sogeti.android.gpstracker.v2.BuildConfig
import timber.log.Timber
import java.io.FileOutputStream

class GpxShareProvider : ContentProvider() {
    companion object {

        val TRACK_MIME_TYPE = "application/gpx+xml"
        val AUTHORITY = BuildConfig.APPLICATION_ID + ".gpxshareprovider"
        val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        private val TRACK_ID = 1

        init {
            uriMatcher.addURI(AUTHORITY, "tracks/#", TRACK_ID)
        }

    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri?): String? {
        var type: String? = null
        val match = uriMatcher.match(uri)
        if (match == TRACK_ID) {
            type = TRACK_MIME_TYPE
        }

        return type
    }

    override fun openFile(uri: Uri?, mode: String?): ParcelFileDescriptor? {
        var file: ParcelFileDescriptor? = null
        val match = uriMatcher.match(uri)
        if (match == TRACK_ID) {
            file = openPipeHelper(uri, TRACK_MIME_TYPE, null, null,
                    { output, shareUri, _, _, _ ->
                        val outputstream = FileOutputStream(output.fileDescriptor)
                        val trackUri = trackUri(shareUri.lastPathSegment.toLong())
                        val gpxCreator = GpxCreator(context, trackUri)
                        gpxCreator.createGpx(outputstream)
                    })
        }

        return file
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri? {
        Timber.e("Insert not supported")
        return null
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        Timber.e("Query not supported")
        return null
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        Timber.e("Update not supported")
        return 0
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        Timber.e("Delete not supported")
        return 0
    }
}