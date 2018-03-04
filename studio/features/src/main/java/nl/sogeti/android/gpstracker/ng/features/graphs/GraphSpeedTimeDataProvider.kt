package nl.sogeti.android.gpstracker.ng.features.graphs

import android.content.Context
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphValueDescriptor
import nl.sogeti.android.gpstracker.ng.features.summary.Summary
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class GraphSpeedOVerDistanceDataProvider : GraphValueDescriptor, GraphDataProvider {

    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter

    override val yLabel: Int
        get() = R.string.graph_label_speed

    override val xLabel: Int
        get() = R.string.graph_label_distance

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun calculateGraphPoints(summary: Summary): List<GraphPoint> {
        return calculateTrack(summary.deltas)
    }

    override val valueDescriptor: GraphValueDescriptor
        get() = this

    override fun describeYvalue(context: Context, yValue: Float): String {
        return statisticsFormatter.convertMeterPerSecondsToSpeed(context, yValue, 1)
    }

    override fun describeXvalue(context: Context, xValue: Float): String {
        return statisticsFormatter.convertMetersToDistance(context, xValue)
    }

    private fun calculateTrack(waypoints: List<List<Summary.Delta>>): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        waypoints.forEach {
            val points = calculateSegment(it)
            list.add(GraphPoint(it.first().distance, 0f))
            list.addAll(points)
            list.add(GraphPoint(it.last().distance, 0f))
        }

        return list
    }

    private fun calculateSegment(deltas: List<Summary.Delta>): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        deltas.forEach {
            val speed = it.meters / (it.duration / 1000F)
            if (speed >= 0F && it.meters > 0F) {
                list.add(GraphPoint(it.distance - it.meters, speed))
                list.add(GraphPoint(it.distance, speed))
            }
        }
        return list
    }

}

