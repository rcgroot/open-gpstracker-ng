/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
-
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.gpxexport

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import nl.sogeti.android.gpstracker.ng.gpxexport.tasks.GpxCreator
import nl.sogeti.android.gpstracker.ng.utils.trackUri
import nl.sogeti.android.gpstracker.v2.BuildConfig
import timber.log.Timber
import java.io.FileOutputStream

const val MIME_TYPE_GPX = "application/gpx+xml"
const val MIME_TYPE_GENERAL = "application/octet-stream"

class GpxShareProvider : ContentProvider() {

    companion object {
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
            type = MIME_TYPE_GPX
        }

        return type
    }

    override fun openFile(uri: Uri?, mode: String?): ParcelFileDescriptor? {
        var file: ParcelFileDescriptor? = null
        val match = uriMatcher.match(uri)
        if (match == TRACK_ID) {
            file = openPipeHelper(uri, MIME_TYPE_GPX, null, null,
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
