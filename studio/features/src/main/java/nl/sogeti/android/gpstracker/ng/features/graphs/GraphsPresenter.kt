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
package nl.sogeti.android.gpstracker.ng.features.graphs

import android.net.Uri
import nl.sogeti.android.gpstracker.ng.base.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.tracklist.summary.Summary
import nl.sogeti.android.gpstracker.ng.features.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.AbstractTrackPresenter
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import javax.inject.Inject

class GraphsPresenter : AbstractTrackPresenter(), TrackSelection.Listener {

    @Inject
    lateinit var summaryManager: SummaryManager
    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter
    internal val viewModel = GraphsViewModel()


    init {
        FeatureConfiguration.featureComponent.inject(this)
        resetTrack()
    }

    override fun onStart() {
        summaryManager.start()
    }

    override fun onStop() {
        summaryManager.stop()
    }

    //region update

    private fun resetTrack() {
        viewModel.distance.set(0F)
        viewModel.timeSpan.set(0L)
        viewModel.speed.set(0F)
        viewModel.waypoints.set("-")
        viewModel.startTime.set(0L)
        viewModel.duration.set(0L)
        viewModel.paused.set(0L)
    }

    override fun onTrackUpdate(trackUri: Uri, name: String) {
        viewModel.trackUri.set(trackUri)
        summaryManager.collectSummaryInfo(trackUri) {
            fillSummaryNumbers(it)
            fillSpeedToTimeGraph(it)
        }
    }

    private fun fillSummaryNumbers(summary: Summary) {
        viewModel.waypoints.set(summary.count.toString())
        viewModel.startTime.set(summary.startTimestamp)
        val pausedTime = (summary.stopTimestamp - summary.startTimestamp) - summary.trackedPeriod
        viewModel.paused.set(pausedTime)
        viewModel.distance.set(summary.distance)
        viewModel.duration.set(summary.trackedPeriod)
        viewModel.timeSpan.set(summary.stopTimestamp - summary.startTimestamp)

        val seconds = summary.trackedPeriod / 1000F
        val speed = if (seconds > 0) summary.distance / seconds else 0F
        viewModel.speed.set(speed)
    }

    private fun fillSpeedToTimeGraph(it: Summary) {
        //    private val graphDataProvider: GraphDataProvider = GraphSpeedTimeDataProvider()
        val graphDataProvider: GraphDataProvider = GraphDistanceTimeDataProvider()
        viewModel.speedAtTimeData.set(graphDataProvider.calculateGraphPoints(it.waypoints))
        viewModel.speedValueDescription.set(graphDataProvider.valueDescriptor)
    }
}

