package nl.sogeti.android.gpstracker.ng.features.graphs

import android.content.Context
import android.support.annotation.WorkerThread
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphValueDescriptor
import nl.sogeti.android.gpstracker.ng.features.model.Preferences
import nl.sogeti.android.gpstracker.ng.features.model.valueOrFalse
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

    @Inject
    lateinit var preferences: Preferences

    @Inject
    lateinit var graphSpeedConverter: GraphSpeedConverter

    private val inverseSpeed
        get() = preferences.inverseSpeed.valueOrFalse()

    override val yLabel: Int
        get() = R.string.graph_label_speed

    override val xLabel: Int
        get() = R.string.graph_label_time

    override val valueDescriptor: GraphValueDescriptor
        get() = this

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    @WorkerThread
    override fun calculateGraphPoints(summary: Summary): List<GraphPoint> {
        val list = if (summary.deltas.firstOrNull()?.firstOrNull() != null) {
            calculateTrack(summary.deltas)
        } else {
            listOf()
        }

        return list
    }


    override fun describeYvalue(context: Context, yValue: Float): String {
        return statisticsFormatter.convertMeterPerSecondsToSpeed(context, yValue.toSpeed(), inverseSpeed)
    }

    override fun describeXvalue(context: Context, xValue: Float): String {
        return statisticsFormatter.convertSpanDescriptiveDuration(context, xValue.toLong())
    }

    private fun calculateTrack(waypoints: List<List<Summary.Delta>>): List<GraphPoint> {
        if (inverseSpeed) {
            val speeds = waypoints.flatMap {
                it.map { it.deltaMeters to (it.deltaMilliseconds / 1000F) }
            }
            graphSpeedConverter.calculateMinMax(speeds)
        }

        val list = mutableListOf<GraphPoint>()
        val baseTime = waypoints.first().first().totalMilliseconds
        fun Long.toX() = (this - baseTime).toFloat()
        waypoints.forEach {
            val points = calculateSegment(it, baseTime)
            list.add(GraphPoint(it.first().totalMilliseconds.toX(), 0f.toY()))
            list.addAll(points)
            list.add(GraphPoint(it.last().totalMilliseconds.toX(), 0f.toY()))
        }

        return list
    }

    fun calculateSegment(deltas: List<Summary.Delta>, baseTime: Long): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        fun Long.toX() = (this - baseTime).toFloat()
        deltas.forEach {
            val speed = it.deltaMeters / (it.deltaMilliseconds / 1000F)
            if (speed >= 0F) {
                list.add(GraphPoint((it.totalMilliseconds - it.deltaMilliseconds).toX(), speed.toY()))
                list.add(GraphPoint(it.totalMilliseconds.toX(), speed.toY()))
            }
        }
        return list
    }

    fun Float.toY() =
            if (inverseSpeed) {
                graphSpeedConverter.speedToYValue(this)
            } else {
                this
            }

    fun Float.toSpeed() =
            if (inverseSpeed) {
                graphSpeedConverter.yValueToSpeed(this)
            } else {
                this
            }

}
