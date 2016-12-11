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

import android.databinding.ObservableField
import android.net.Uri
import android.os.AsyncTask
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import nl.sogeti.android.gpstracker.integration.ServiceConstants
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.TrackObservingPresenter
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackTileProvider
import nl.sogeti.android.gpstracker.ng.utils.DefaultResultHandler
import nl.sogeti.android.gpstracker.ng.utils.ResultHandler
import nl.sogeti.android.gpstracker.ng.utils.readTrack
import nl.sogeti.android.gpstracker.ng.utils.trackUri
import java.util.*

class TrackPresenter(private val viewModel: TrackViewModel) : TrackObservingPresenter(), OnMapReadyCallback {
    private var isReading: Boolean = false

    override fun didStart() {
        super.didStart()
        val trackUri = viewModel.trackUri.get()
        if (trackUri != null) {
            TrackReader(trackUri, viewModel).execute()
        }
    }

    /* Service connecting */

    override fun didConnectService(serviceManager: ServiceManagerInterface) {
        val loggingState = serviceManager.loggingState
        val trackId = serviceManager.trackId
        val uri = trackUri(trackId)
        updateRecording(uri, loggingState)
    }

    override fun didChangeLoggingState(uri: Uri, loggingState: Int) {
        updateRecording(uri, loggingState)
    }

    /* Content watching */

    override fun getTrackUriField(): ObservableField<Uri?> {
        return viewModel.trackUri
    }

    override fun onChangeUriField(uri: Uri) {
        TrackReader(uri, viewModel).execute()
    }

    override fun onChangeUriContent(uri: Uri){
        val isCurrentTrack = uri == viewModel.trackUri.get()
        if (!isReading && isCurrentTrack) {
            TrackReader(uri, viewModel).execute()
        }
    }

    /* Google Map Tiles */

    override fun onMapReady(googleMap: GoogleMap) {
        val tileProvider = TrackTileProvider(context, viewModel.waypoints)
        tileProvider.provideFor(googleMap)
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
