/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.ng.graphs

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.v2.R
import javax.inject.Inject

class GraphsPresenter : ContextedPresenter(), TrackSelection.Listener {

    @Inject
    lateinit var summaryManager: SummaryManager
    @Inject
    lateinit var calculator: SummaryCalculator
    val viewModel = GraphsViewModel()
    @Inject
    lateinit var trackSelection: TrackSelection

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    override fun didStart() {
        trackSelection.addListener(this)
        summaryManager.start()
        val trackUri = trackSelection.trackUri
        if (trackUri != null) {
            setTrack(trackUri)
        }
    }

    override fun willStop() {
        trackSelection.removeListener(this)
        summaryManager.stop()
    }

    //region TrackSelection.Listener

    override fun onTrackSelection(trackUri: Uri, name: String) {
        setTrack(trackUri)
    }

    //endregion

    private fun setTrack(trackUri: Uri) {
        viewModel.trackUri.set(trackUri)
        viewModel.distance.set("-")
        viewModel.time.set("-")
        viewModel.speed.set("-")
        viewModel.waypoints.set("-")
        viewModel.startDate.set("-")
        viewModel.startTime.set("-")
        viewModel.total.set("-")
        viewModel.paused.set("-")
        summaryManager.collectSummaryInfo(context, trackUri) {
            viewModel.waypoints.set(it.count.toString())

            var speed = context.getString(R.string.row_distance_default)
            if (it.trackedPeriod > 0 && it.distance > 0) {
                speed = calculator.convertMeterPerSecondsToSpeed(context, it.distance, it.trackedPeriod / 1000)
            }
            viewModel.speed.set(speed)

            var distance = context.getString(R.string.row_distance_default)
            if (it.distance > 0) {
                distance = calculator.convertMetersToDistance(context, it.distance)
            }
            viewModel.distance.set(distance)

            var tracked = context.getString(R.string.row_distance_default)
            if (it.trackedPeriod > 0) {
                tracked = calculator.convertStartEndToDuration(context, 0, it.trackedPeriod)
            }
            viewModel.time.set(tracked)

            var duration = context.getString(R.string.row_duraction_default)
            if (it.startTimestamp in 1..(it.stopTimestamp - 1)) {
                duration = calculator.convertStartEndToDuration(context, it.startTimestamp, it.stopTimestamp)
            }
            viewModel.total.set(duration)

            var pause = context.getString(R.string.row_distance_default)
            val pausedTime = (it.stopTimestamp - it.startTimestamp) - it.trackedPeriod
            if (pausedTime > 0) {
                pause = calculator.convertStartEndToDuration(context, 0, pausedTime)
            }
            viewModel.paused.set(pause)

            viewModel.startDate.set(calculator.convertTimestampToDate(context, it.startTimestamp))
            viewModel.startTime.set(calculator.convertTimestampToTime(context, it.startTimestamp))
        }
    }

    private fun calculateSpeedGraph(waypoints: List<List<LatLng>>) {

    }

}
