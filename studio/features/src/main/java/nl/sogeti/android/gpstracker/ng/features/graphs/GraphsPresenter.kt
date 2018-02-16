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

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.LineGraph
import nl.sogeti.android.gpstracker.ng.features.tracklist.summary.Summary
import nl.sogeti.android.gpstracker.ng.features.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.features.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.service.util.Waypoint
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import javax.inject.Inject

class GraphsPresenter : ContextedPresenter(), TrackSelection.Listener {

    @Inject
    lateinit var summaryManager: SummaryManager
    @Inject
    lateinit var calculator: SummaryCalculator
    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter
    @Inject
    lateinit var trackSelection: TrackSelection
    val viewModel = GraphsViewModel()

    class SpeedValuesDescriptor(val statisticsFormatter: StatisticsFormatter) : LineGraph.ValueDescriptor {
        override fun describeYvalue(context: Context, yValue: Float): String {
            // Y value speed in the graph is meter per millisecond
            return statisticsFormatter.convertMeterPerSecondsToSpeed(context, yValue * 1000f, 1)
        }

        override fun describeXvalue(context: Context, xValue: Float): String {
            return statisticsFormatter.convertSpanDescriptiveDuration(context, xValue.toLong())
        }
    }

    init {
        FeatureConfiguration.featureComponent.inject(this)
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
        viewModel.distance.set(0F)
        viewModel.timeSpan.set(0L)
        viewModel.speed.set(0F)
        viewModel.waypoints.set("-")
        viewModel.startTime.set(0L)
        viewModel.duration.set(0L)
        viewModel.paused.set(0L)
        viewModel.speedValueDescription.set(SpeedValuesDescriptor(statisticsFormatter))
        summaryManager.collectSummaryInfo(context, trackUri) {
            fillSummaryNumbers(it)
            fillSpeedToTimeGraph(it)
        }
    }

    private fun fillSpeedToTimeGraph(it: Summary) {
        val graphPoints = calculateSpeedGraph(it.waypoints, it.startTimestamp, it.stopTimestamp)
        viewModel.speedAtTimeData.set(graphPoints)
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

    private fun calculateSpeedGraph(waypoints: List<List<Waypoint>>, start: Long, stop: Long): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        waypoints.forEach {
            list.add(GraphPoint((it.first().time - start).toFloat(), 0f))
            val points = calculateSpeedGraphSegment(it, start)
            list.addAll(points)
            list.add(GraphPoint((it.last().time - start).toFloat(), 0f))
        }

        return list
    }

    fun calculateSpeedGraphSegment(waypoints: List<Waypoint>, start: Long): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()

        val outArray = floatArrayOf(0.0F)
        val deltas = waypoints.forDelta { first, second ->
            val deltaDuration = second.time - first.time
            val deltaDistance = calculator.distance(first, second, outArray)
            Delta(first.time, second.time, deltaDistance / deltaDuration)
        }
        fun Long.toX() = (this - start).toFloat()
        deltas.forEach {
            list.add(GraphPoint(it.startTime.toX(), it.speed))
            list.add(GraphPoint(it.endTime.toX(), it.speed))
        }

        return list
    }

    data class Delta(val startTime: Long, val endTime: Long, val speed: Float)
}

inline fun <T, R> List<T>.forDelta(delta: (T, T) -> R): List<R> {
    return (0 until count() - 1).map { delta(this[it], this[it + 1]) }
}
