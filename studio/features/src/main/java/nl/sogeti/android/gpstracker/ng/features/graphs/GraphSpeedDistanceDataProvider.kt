package nl.sogeti.android.gpstracker.ng.features.graphs

import android.content.Context
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.LineGraph
import nl.sogeti.android.gpstracker.ng.features.summary.Summary
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.service.util.Waypoint
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class GraphSpeedOverTimeDataProvider : LineGraph.ValueDescriptor, GraphDataProvider {

    @Inject
    lateinit var calculator: SummaryCalculator

    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter

    override val yLabel: Int
        get() = R.string.graph_label_speed

    override val xLabel: Int
        get() = R.string.graph_label_time

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun calculateGraphPoints(summary: Summary): List<GraphPoint> {
        return calculateSpeedGraph(summary.deltas)
    }

    override val valueDescriptor: LineGraph.ValueDescriptor
        get() = this

    override fun describeYvalue(context: Context, yValue: Float): String {
        // Y value speed in the graph is meter per millisecond
        return statisticsFormatter.convertMeterPerSecondsToSpeed(context, yValue * 1000f, 1)
    }

    override fun describeXvalue(context: Context, xValue: Float): String {
        return statisticsFormatter.convertSpanDescriptiveDuration(context, xValue.toLong())
    }

    private fun calculateSpeedGraph(waypoints: List<List<Summary.Delta>>): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        waypoints.forEach {
            list.add(GraphPoint((it.first().time).toFloat(), 0f))
            val points = calculateSpeedGraphSegment(it)
            list.addAll(points)
            list.add(GraphPoint((it.last().time - start).toFloat(), 0f))
        }

        return list
    }

    fun calculateSpeedGraphSegment(waypoints: List<Waypoint>, start: Long): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()

        val outArray = floatArrayOf(0.0F)
        val deltas = waypoints.toDeltas { first, second ->
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
