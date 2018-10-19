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
package nl.sogeti.android.gpstracker.ng.features.gpxexport

import android.content.Intent
import android.net.Uri
import nl.renedegroot.opengpstracker.exporter.gpx.GpxCreator
import nl.renedegroot.opengpstracker.exporter.gpx.GpxCreator.MIME_TYPE_GPX
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration.featureComponent
import nl.sogeti.android.gpstracker.service.integration.ContentConstants

class ShareIntentFactory {

    fun createShareIntent(track: Uri): Intent {
        val shareIntent = Intent()
        val fileName = GpxCreator.fileName(track)
        val trackId = checkNotNull(track.lastPathSegment).toLong()
        val trackStream = sharedTrackUri(trackId, fileName)
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_STREAM, trackStream)
        shareIntent.type = MIME_TYPE_GPX

        return shareIntent
    }

    /**
     *
     * @param trackId
     * @return uri, for example content://nl.sogeti.android.gpstracker.authority/tracks/5
     */
    private fun sharedTrackUri(trackId: Long, fileName: String): Uri = BaseConfiguration.appComponent.uriBuilder()
            .scheme("content")
            .authority(featureComponent.providerShareAuthority())
            .appendPath(ContentConstants.Tracks.TRACKS)
            .appendEncodedPath(trackId.toString())
            .appendPath(fileName)
            .build()

}
