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
package nl.sogeti.android.gpstracker.ng.features.gpximport

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.DocumentsContract.Document.*
import android.support.annotation.RequiresApi
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.gpxexport.GpxShareProvider.Companion.MIME_TYPE_GPX
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.features.trackedit.saveTrackType
import nl.sogeti.android.gpstracker.ng.features.tracklist.ImportNotification
import nl.sogeti.android.gpstracker.utils.contentprovider.countResult
import nl.sogeti.android.gpstracker.utils.contentprovider.getString
import nl.sogeti.android.gpstracker.utils.contentprovider.map
import timber.log.Timber
import javax.inject.Inject

class GpxImportController(private val context: Context) {

    @Inject
    lateinit var parser: GpxParser
    @Inject
    lateinit var notification: ImportNotification

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    fun import(uri: Uri, trackType: String) {
        notification.didStartImport()
        notification.onProgress(0, 1)
        importTrack(uri, trackType)
        notification.onProgress(1, 1)
        notification.didCompleteImport()
    }

    private fun importTrack(uri: Uri, trackType: String) {
        val defaultName = extractName(uri)
        val trackUri = parser.parseTrack(context.contentResolver.openInputStream(uri), defaultName)
        trackUri.saveTrackType(TrackTypeDescriptions.trackTypeForContentType(trackType))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun importDirectory(uri: Uri, trackType: String) {
        notification.didStartImport()
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
        val projection = listOf(COLUMN_DOCUMENT_ID, COLUMN_MIME_TYPE, COLUMN_DISPLAY_NAME)
        val count = childrenUri.countResult(context.contentResolver)
        var progress = 0
        notification.onProgress(progress, count)
        childrenUri.map(context.contentResolver, projection = projection) {
            val id = it.getString(COLUMN_DOCUMENT_ID)
            val mimeType = it.getString(COLUMN_MIME_TYPE)
            val name = it.getString(COLUMN_DISPLAY_NAME)
            if (mimeType == MIME_TYPE_GPX || name?.endsWith(".gpx", true) == true) {
                importTrack(DocumentsContract.buildDocumentUriUsingTree(uri, id), trackType)
            } else {
                Timber.e("Will not import file $name")
            }
            progress++
            notification.onProgress(progress, count)
        }
        notification.didCompleteImport()
    }

    private fun extractName(uri: Uri): String {
        val startIndex = uri.lastPathSegment.indexOfLast { it == '/' }

        return if (startIndex != -1) {
            uri.lastPathSegment.substring(startIndex + 1).removeSuffix(".gpx")
        } else {
            uri.lastPathSegment.removeSuffix(".gpx")
        }
    }
}

