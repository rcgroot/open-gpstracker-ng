package nl.sogeti.android.gpstracker.ng.features.graphs

import android.content.Context
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphValueDescriptor
import nl.sogeti.android.gpstracker.ng.features.summary.Summary
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class GraphSpeedOverTimeDataProvider : GraphValueDescriptor, GraphDataProvider {

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
        val list = if (summary.deltas.firstOrNull()?.firstOrNull() != null) {
            calculateTrack(summary.deltas)
        } else {
            listOf()
        }

        return list
    }

    override val valueDescriptor: GraphValueDescriptor
        get() = this

    override fun describeYvalue(context: Context, yValue: Float): String {
        return statisticsFormatter.convertMeterPerSecondsToSpeed(context, yValue)
    }

    override fun describeXvalue(context: Context, xValue: Float): String {
        return statisticsFormatter.convertSpanDescriptiveDuration(context, xValue.toLong())
    }

    private fun calculateTrack(waypoints: List<List<Summary.Delta>>): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        val baseTime = waypoints.first().first().time
        fun Long.toX() = (this - baseTime).toFloat()
        waypoints.forEach {
            val points = calculateSegment(it, baseTime)
            list.add(GraphPoint(it.first().time.toX(), 0f))
            list.addAll(points)
            list.add(GraphPoint(it.last().time.toX(), 0f))
        }

        return list
    }

    fun calculateSegment(deltas: List<Summary.Delta>, baseTime: Long): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        fun Long.toX() = (this - baseTime).toFloat()
        deltas.forEach {
            val speed = it.meters / (it.duration / 1000F)
            if (speed >= 0F) {
                list.add(GraphPoint((it.time - it.duration).toX(), speed))
                list.add(GraphPoint(it.time.toX(), speed))
            }
        }
        return list
    }

}
