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

import android.content.Context
import android.net.Uri
import android.provider.BaseColumns
import androidx.annotation.WorkerThread
import androidx.lifecycle.Observer
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.dagger.DiskIO
import nl.sogeti.android.gpstracker.ng.base.location.LocationFactory
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.map.rendering.TrackTileProvider
import nl.sogeti.android.gpstracker.ng.features.map.rendering.TrackTileProviderFactory
import nl.sogeti.android.gpstracker.ng.features.model.Preferences
import nl.sogeti.android.gpstracker.ng.features.model.not
import nl.sogeti.android.gpstracker.ng.features.model.valueOrFalse
import nl.sogeti.android.gpstracker.ng.features.util.AbstractSelectedTrackPresenter
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateController
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateListener
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants
import nl.sogeti.android.gpstracker.service.util.trackUri
import nl.sogeti.android.gpstracker.service.util.tracksUri
import nl.sogeti.android.gpstracker.utils.contentprovider.getLong
import nl.sogeti.android.gpstracker.utils.contentprovider.runQuery
import java.util.concurrent.Executor
import javax.inject.Inject

class TrackMapPresenter : AbstractSelectedTrackPresenter(), OnMapReadyCallback, ContentController.Listener, LoggingStateListener {

    @Inject
    lateinit var trackReaderFactory: TrackReaderFactory
    @Inject
    lateinit var trackTileProviderFactory: TrackTileProviderFactory
    @Inject
    lateinit var locationFactory: LocationFactory
    @Inject
    lateinit var loggingStateController: LoggingStateController
    @Inject
    lateinit var preferences: Preferences
    @field:DiskIO
    @Inject
    lateinit var executor: Executor
    private var executingReader: TrackReader? = null
    private var tileProvider: TrackTileProvider? = null
    internal val viewModel = TrackMapViewModel()
    private val wakelockPreferenceObserver = Observer<Boolean> {
        viewModel.willLock.set(it ?: false)
        updateLock()
    }
    private val satellitePreferenceObserver = Observer<Boolean> {
        viewModel.showSatellite.set(it ?: false)
    }

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun onFirstStart() {
        super.onFirstStart()
        preferences.wakelockScreen.observeForever(wakelockPreferenceObserver)
        preferences.satellite.observeForever(satellitePreferenceObserver)
        loggingStateController.connect(this)
        executor.execute { makeTrackSelection() }
    }

    fun start(mapView: MapView) {
        super.start()
        tileProvider = trackTileProviderFactory.createTrackTileProvider(mapView.context, viewModel.waypoints)
        mapView.getMapAsync(this)
    }

    override fun onStop() {
        tileProvider = null
        super.onStop()
    }

    override fun onCleared() {
        preferences.wakelockScreen.removeObserver(wakelockPreferenceObserver)
        preferences.satellite.removeObserver(satellitePreferenceObserver)
        loggingStateController.disconnect()
        super.onCleared()
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

    //region Service connecting

    override fun didConnectToService(context: Context, loggingState: Int, trackUri: Uri?) {
        didChangeLoggingState(context, loggingState, trackUri)
    }

    override fun didChangeLoggingState(context: Context, loggingState: Int, trackUri: Uri?) {
        val isLogging = loggingState == ServiceConstants.STATE_LOGGING
        updateLock()
        if (isLogging && trackUri != null) {
            trackSelection.selection.value = trackUri
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

    fun onSatelliteSelected() {
        preferences.satellite.not()
    }

    fun onScreenLockSelected() {
        preferences.wakelockScreen.not()
    }

    /* Private */

    private fun updateLock() {
        val isLogging = loggingStateController.loggingState == ServiceConstants.STATE_LOGGING
        val shouldLock = preferences.wakelockScreen.valueOrFalse()
        viewModel.isLocked.set(isLogging && shouldLock)
    }

    private fun startReadingTrack(trackUri: Uri) {
        var executingReader = this.executingReader
        if ((executingReader == null || executingReader.isFinished || executingReader.trackUri != trackUri)) {
            executingReader?.cancel(true)
            executingReader = trackReaderFactory.createTrackReader(trackUri) { name, bounds, waypoint ->
                viewModel.name.set(name)
                viewModel.waypoints.set(waypoint)
                if (loggingStateController.loggingState == ServiceConstants.STATE_LOGGING) {
                    viewModel.completeBounds.set(null)
                    viewModel.trackHead.set(waypoint.lastOrNull()?.lastOrNull())
                } else {
                    viewModel.trackHead.set(null)
                    viewModel.completeBounds.set(bounds)
                }
            }
            this.executingReader = executingReader
            executingReader.execute()
        }
    }

    @WorkerThread
    private fun makeTrackSelection() {
        val selectedTrack = trackSelection.selection.value
        if (selectedTrack == null || selectedTrack.lastPathSegment != "-1") {
            val lastTrack = tracksUri().runQuery(BaseConfiguration.appComponent.contentResolver()) { it.moveToLast(); it.getLong(BaseColumns._ID) }
            if (lastTrack != null) {
                val lastTrackUri = trackUri(lastTrack)
                trackSelection.selection.postValue(lastTrackUri)
            } else {
                viewModel.trackHead.set(locationFactory.getLocationCoordinates())
            }
        }
    }
}

