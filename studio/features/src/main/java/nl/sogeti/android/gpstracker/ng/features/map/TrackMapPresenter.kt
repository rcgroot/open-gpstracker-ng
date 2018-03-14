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
package nl.sogeti.android.gpstracker.ng.features.map

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.location.LocationFactory
import nl.sogeti.android.gpstracker.ng.base.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.map.rendering.TrackTileProvider
import nl.sogeti.android.gpstracker.ng.features.map.rendering.TrackTileProviderFactory
import nl.sogeti.android.gpstracker.ng.features.util.AbstractSelectedTrackPresenter
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateController
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateListener
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.TracksColumns.NAME
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants
import nl.sogeti.android.gpstracker.service.util.trackUri
import nl.sogeti.android.gpstracker.service.util.tracksUri
import nl.sogeti.android.gpstracker.utils.contentprovider.getLong
import nl.sogeti.android.gpstracker.utils.contentprovider.getString
import nl.sogeti.android.gpstracker.utils.contentprovider.runQuery
import javax.inject.Inject

class TrackMapPresenter @Inject constructor(
        private val trackReaderFactory: TrackReaderFactory,
        private val trackTileProviderFactory: TrackTileProviderFactory,
        private val locationFactory: LocationFactory,
        private val loggingStateController: LoggingStateController,
        trackSelection: TrackSelection,
        contentController: ContentController)
    : AbstractSelectedTrackPresenter(trackSelection, contentController), OnMapReadyCallback, ContentController.Listener, TrackSelection.Listener, LoggingStateListener {

    private var executingReader: TrackReader? = null

    internal val viewModel = TrackMapViewModel()

    init {
        loggingStateController.connect(this)
        makeTrackSelection()
    }

    private var tileProvider: TrackTileProvider? = null

    fun start(mapView: MapView) {
        super.start()
        tileProvider = trackTileProviderFactory.createTrackTileProvider(mapView.context, viewModel.waypoints)
        mapView.getMapAsync(this)
    }

    override fun onTrackUpdate(trackUri: Uri?, name: String) {
        viewModel.trackUri.set(trackUri)
        viewModel.name.set(name)
        if (trackUri != null) {
            startReadingTrack(trackUri)
        } else {
            viewModel.name.set(name)
            viewModel.waypoints.set(emptyList())
            viewModel.completeBounds.set(null)
            viewModel.trackHead.set(null)
        }
    }

    override fun onStop() {
        tileProvider = null
        super.onStop()
    }

    override fun onCleared() {
        loggingStateController.disconnect()
        super.onCleared()
    }

    //region Service connecting

    override fun didConnectToService(context: Context, trackUri: Uri?, name: String?, loggingState: Int) {
        didChangeLoggingState(context, trackUri, name, loggingState)
    }

    override fun didChangeLoggingState(context: Context, trackUri: Uri?, name: String?, loggingState: Int) {
        if (loggingState == ServiceConstants.STATE_LOGGING && trackUri != null) {
            trackSelection.selectTrack(trackUri, name ?: "")
        }
    }

    //endregion

    //region Google Map Tiles

    override fun onMapReady(googleMap: GoogleMap) {
        tileProvider?.provideFor(googleMap)
    }

    //endregion

    //region View callbacks

    fun onClickMyLocation() {
        viewModel.trackHead.set(locationFactory.getLocationCoordinates())
        viewModel.completeBounds.set(null)
    }

    /* Private */

    private fun startReadingTrack(trackUri: Uri) {
        var executingReader = this.executingReader
        if ((executingReader == null || executingReader.isFinished || executingReader.trackUri != trackUri)) {
            executingReader?.cancel(true)
            executingReader = trackReaderFactory.createTrackReader(trackUri, { name, bounds, waypoint ->
                viewModel.name.set(name)
                viewModel.waypoints.set(waypoint)
                if (loggingStateController.lastState == ServiceConstants.STATE_LOGGING) {
                    viewModel.completeBounds.set(null)
                    viewModel.trackHead.set(waypoint.lastOrNull()?.lastOrNull())
                } else {
                    viewModel.trackHead.set(null)
                    viewModel.completeBounds.set(bounds)
                }
            })
            this.executingReader = executingReader
            executingReader.execute()
        }
    }

    private fun makeTrackSelection() {
        val selectedTrack = trackSelection.trackUri
        if (selectedTrack != null && selectedTrack.lastPathSegment != "-1") {
            onTrackSelection(selectedTrack, trackSelection.trackName)
        } else {
            val lastTrack = tracksUri().runQuery(BaseConfiguration.appComponent.contentResolver()) { it.moveToLast(); Pair(it.getLong(BaseColumns._ID), it.getString(NAME)) }
            if (lastTrack?.first != null) {
                val trackId = lastTrack.first!!
                val lastTrackUri = trackUri(trackId)
                val name = lastTrack.second ?: ""
                trackSelection.selectTrack(lastTrackUri, name)
            } else {
                viewModel.trackHead.set(locationFactory.getLocationCoordinates())
            }
        }
    }


    @Suppress("UNCHECKED_CAST")
    companion object {

        fun newFactory() =
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        val presenter = FeatureConfiguration.featureComponent.trackMapPresenter()
                        return presenter as T
                    }
                }
    }
}

