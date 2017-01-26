/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
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
package nl.sogeti.android.gpstracker.ng.map

import android.net.Uri
import android.os.AsyncTask
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import nl.sogeti.android.gpstracker.integration.ServiceConstants
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ConnectedServicePresenter
import nl.sogeti.android.gpstracker.ng.common.controllers.ContentController
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackTileProvider
import nl.sogeti.android.gpstracker.ng.utils.DefaultResultHandler
import nl.sogeti.android.gpstracker.ng.utils.readTrack
import nl.sogeti.android.gpstracker.ng.utils.trackUri
import java.lang.ref.WeakReference

class TrackPresenter(private val viewModel: TrackViewModel) : ConnectedServicePresenter(), OnMapReadyCallback, ContentController.ContentListener {

    private var isReading: Boolean = false
    private var contentController: ContentController? = null
    private var weakGoogleMap = WeakReference<GoogleMap?>(null)

    override fun didStart() {
        super.didStart()
        contentController = ContentController(context!!, viewModel.trackUri, this)
        val trackUri = viewModel.trackUri.get()
        if (trackUri != null && trackUri.lastPathSegment != "-1") {
            TrackReader(trackUri, viewModel).execute()
        }
        addTilesToMap()
    }

    override fun willStop() {
        super.willStop()
        contentController?.destroy()
    }

    /* Service connecting */

    override fun didChangeLoggingState(uri: Uri, loggingState: Int) {
        updateRecording(uri, loggingState)
    }

    /* Content watching */

    override fun onChangeUriField(uri: Uri) {
        TrackReader(uri, viewModel).execute()
    }

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        if (!isReading) {
            TrackReader(contentUri, viewModel).execute()
        }
    }

    /* Google Map Tiles */

    override fun onMapReady(googleMap: GoogleMap) {
        weakGoogleMap = WeakReference(googleMap)
        addTilesToMap()
    }

    private fun addTilesToMap() {
        val googleMap = weakGoogleMap.get()
        val context = this.context
        if (googleMap != null && context != null) {
            val tileProvider = TrackTileProvider(context, viewModel.waypoints)
            tileProvider.provideFor(googleMap)
        }
    }

    /* Private */

    private fun updateRecording(trackUri: Uri?, loggingState: Int) {
        if (trackUri != null && trackUri == viewModel.trackUri.get()) {
            viewModel.isRecording.set(loggingState == ServiceConstants.STATE_LOGGING)
        }
    }

    inner class TrackReader internal constructor(private val trackUri: Uri, private val viewModel: TrackViewModel)
        : AsyncTask<Void, Void, Void>() {

        val handler = DefaultResultHandler()

        override fun onPreExecute() {
            isReading = true
        }

        override fun doInBackground(vararg p: Void): Void? {
            context?.let {
                trackUri.readTrack(it, handler, null)
            }
            return null
        }

        override fun onPostExecute(result: Void?) {
            viewModel.name.set(handler.name)

            var builder = handler.headBuilder
            if (builder != null) {
                viewModel.trackHeadBounds.set(builder.build())
            }
            builder = handler.boundsBuilder
            if (builder != null) {
                viewModel.completeBounds.set(builder.build())
            }
            viewModel.waypoints.set(handler.waypoints)
            isReading = false
        }
    }
}
