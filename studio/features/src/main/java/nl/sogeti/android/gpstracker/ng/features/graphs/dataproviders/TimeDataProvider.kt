package nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders

import android.content.Context
import androidx.annotation.WorkerThread
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphSpeedConverter
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.summary.Summary
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class TimeDataProvider(private val inverseSpeed: Boolean) : GraphDataCalculator {

    @Inject
    lateinit var calculator: SummaryCalculator

    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter

    @Inject
    lateinit var graphSpeedConverter: GraphSpeedConverter

    val speedRangePicker = SpeedRangePicker(inverseSpeed)

    override val yLabel: Int
        get() = R.string.graph_label_speed

    override val xLabel: Int
        get() = R.string.graph_label_time

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    override fun prettyMinYValue(context: Context, yValue: Float) =
            speedRangePicker.prettyMinYValue(context, yValue)

    override fun prettyMaxYValue(context: Context, yValue: Float) =
            speedRangePicker.prettyMaxYValue(context, yValue)

    @WorkerThread
    override fun calculateGraphPoints(summary: Summary): List<GraphPoint> {
        val graphPoints = mutableListOf<GraphPoint>()
        addSegmentToGraphPoints(summary.deltas, graphPoints)

        val milliseconds = 40_000F
        return graphPoints
                .inverseSpeed()
                .flattenOutliers()
                .smooth(milliseconds)
    }


    override fun describeYvalue(context: Context, yValue: Float): String {
        val speed = if (inverseSpeed) yValue.inverseSpeed() else yValue
        return statisticsFormatter.convertMeterPerSecondsToSpeed(context, speed, inverseSpeed)
    }

    override fun describeXvalue(context: Context, xValue: Float): String {
        return statisticsFormatter.convertSpanDescriptiveDuration(context, xValue.toLong())
    }

    private fun addSegmentToGraphPoints(waypoints: List<List<Summary.Delta>>, graphPoints: MutableList<GraphPoint>) {
        val baseTime = waypoints.firstOrNull()?.firstOrNull()?.totalMilliseconds ?: 0L
        waypoints.forEach {
            val points = calculateSegment(it, baseTime)
            graphPoints.addAll(points)
        }
    }

    fun calculateSegment(deltas: List<Summary.Delta>, baseTime: Long): List<GraphPoint> {
        val list = mutableListOf<GraphPoint>()
        fun Long.toX() = (this - baseTime).toFloat()
        deltas.forEach {
            val speed = it.deltaMeters / (it.deltaMilliseconds / 1000F)
            if (speed >= 0F) {
                val moment = it.totalMilliseconds - (it.deltaMilliseconds / 2L)
                list.add(GraphPoint(moment.toX(), speed))
            }
        }
        return list
    }

    private fun List<GraphPoint>.inverseSpeed(): List<GraphPoint> {
        return if (inverseSpeed) {
            this.map { GraphPoint(it.x, it.y.inverseSpeed()) }
        } else {
            this
        }
    }

    private fun Float.inverseSpeed() = graphSpeedConverter.speedToYValue(this)


}
