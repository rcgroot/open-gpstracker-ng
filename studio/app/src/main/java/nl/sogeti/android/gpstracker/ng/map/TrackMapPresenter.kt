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
import android.provider.BaseColumns
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import nl.sogeti.android.gpstracker.integration.ContentConstants.TracksColumns.NAME
import nl.sogeti.android.gpstracker.integration.ServiceConstants
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ConnectedServicePresenter
import nl.sogeti.android.gpstracker.ng.common.controllers.ContentController
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackTileProvider
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.utils.*
import java.lang.ref.WeakReference
import javax.inject.Inject

class TrackMapPresenter(private val viewModel: TrackMapViewModel) : ConnectedServicePresenter(), OnMapReadyCallback, ContentController.ContentListener, TrackSelection.Listener {
    private var executingReader: TrackReader? = null

    private var contentController: ContentController? = null
    private var weakGoogleMap = WeakReference<GoogleMap?>(null)
    @Inject
    lateinit var trackSelection: TrackSelection

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    override fun didStart() {
        super.didStart()
        trackSelection.addListener(this)
        makeTrackSelection()
        contentController = ContentController(context!!, this)
        contentController?.registerObserver(viewModel.trackUri.get())
        addTilesToMap()
    }

    override fun willStop() {
        super.willStop()
        trackSelection.removeListener(this)
        contentController?.unregisterObserver()
        contentController = null
    }

    //region Track selection

    override fun didSelectTrack(trackUri: Uri, name: String) {
        viewModel.trackUri.set(trackUri)
        viewModel.name.set(name)
        contentController?.registerObserver(trackUri)
        startReadingTrack(trackUri)
    }

    //endregion

    //region Service connecting

    override fun didConnectToService(trackUri: Uri?, name: String?, loggingState: Int) {
        val isRecording = loggingState == ServiceConstants.STATE_LOGGING
        if (trackUri != null && isRecording && trackUri == viewModel.trackUri.get()) {
            viewModel.isRecording.set(isRecording)
        }
    }

    override fun didChangeLoggingState(trackUri: Uri?, name: String?, loggingState: Int) {
        val isRecording = loggingState == ServiceConstants.STATE_LOGGING
        if (trackUri != null && isRecording) {
            val trackName = name ?: ""
            trackSelection.selectTrack(trackUri, trackName)
            viewModel.isRecording.set(isRecording)
        }
    }

    //endregion

    /* Content watching */

    override fun onChangeUriContent(contentUri: Uri, changesUri: Uri) {
        startReadingTrack(contentUri)
    }

    private fun startReadingTrack(trackUri: Uri) {
        var executingReader = this.executingReader;
        if (executingReader == null || executingReader.trackUri != trackUri) {
            executingReader?.cancel(true)
            executingReader = TrackReader(trackUri, viewModel)
            executingReader.execute()
            this.executingReader = executingReader
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

    private fun makeTrackSelection() {
        val trackUri = trackSelection.trackUri
        if (trackUri != null && trackUri.lastPathSegment != "-1") {
            didSelectTrack(trackUri, trackSelection.trackName)
        } else {
            val context = context
            if (context != null) {
                val lastTrack = tracksUri().apply(context, { it.moveToLast();Pair(it.getLong(BaseColumns._ID), it.getString(NAME)) })
                if (lastTrack != null && lastTrack.first != null) {
                    val trackId = lastTrack.first as Long
                    val lastTrackUri = trackUri(trackId)
                    val name = lastTrack.second ?: ""
                    trackSelection.selectTrack(lastTrackUri, name)
                }
            }
        }
    }

    inner class TrackReader internal constructor(val trackUri: Uri, private val viewModel: TrackMapViewModel)
        : AsyncTask<Void, Void, Void>() {

        val handler = DefaultResultHandler()

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
            if (executingReader == this) {
                executingReader = null
            }
        }
    }
}
