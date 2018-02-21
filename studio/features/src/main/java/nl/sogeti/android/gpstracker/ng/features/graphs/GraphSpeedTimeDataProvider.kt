package nl.sogeti.android.gpstracker.ng.features.graphs

import android.content.Context
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.LineGraph
import nl.sogeti.android.gpstracker.ng.features.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.service.util.Waypoint
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class GraphDistanceTimeDataProvider : LineGraph.ValueDescriptor, GraphDataProvider {

    @Inject
    lateinit var calculator: SummaryCalculator

    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter

    override val yLabel: Int
        get() = R.string.graph_label_speed

    override val xLabel: Int
        get() = R.string.graph_label_distance

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun calculateGraphPoints(waypoints: List<List<Waypoint>>): List<GraphPoint> {
        return calculateSpeedGraph(waypoints)
    }

    override val valueDescriptor: LineGraph.ValueDescriptor
        get() = this

    override fun describeYvalue(context: Context, yValue: Float): String {
        // Y value speed in the graph is meter per millisecond
        return statisticsFormatter.convertMeterPerSecondsToSpeed(context, yValue * 1000f, 1)
    }

    override fun describeXvalue(context: Context, xValue: Float): String {
        return statisticsFormatter.convertMetersToDistance(context, xValue)
    }

    private fun calculateSpeedGraph(waypoints: List<List<Waypoint>>): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        var distance = 0F
        waypoints.forEach {
            list.add(GraphPoint(distance, 0f))
            val points = calculateSpeedGraphSegment(it, distance)
            list.addAll(points)
            distance += points.last().x
            list.add(GraphPoint(distance, 0f))
        }

        return list
    }

    fun calculateSpeedGraphSegment(waypoints: List<Waypoint>, startDistance: Float): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        val outArray = floatArrayOf(0.0F)
        var distance = startDistance
        val deltas = waypoints.toDeltas { first, second ->
            val deltaDuration = second.time - first.time
            val deltaDistance = calculator.distance(first, second, outArray)
            distance += deltaDistance
            Delta(distance - deltaDistance, distance, deltaDistance / deltaDuration)

        }
        deltas.forEach {
            list.add(GraphPoint(it.startDistance, it.speed))
            list.add(GraphPoint(it.endDistance, it.speed))
        }

        return list
    }

    data class Delta(val startDistance: Float, val endDistance: Float, val speed: Float)

}

