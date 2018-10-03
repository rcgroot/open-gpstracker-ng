package nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders

import android.content.Context
import android.support.annotation.WorkerThread
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphSpeedConverter
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.summary.Summary
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class DistanceDataProvider(private val inverseSpeed: Boolean) : GraphDataCalculator {

    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter

    @Inject
    lateinit var graphSpeedConverter: GraphSpeedConverter

    val speedRangePicker = SpeedRangePicker(inverseSpeed)

    override val yLabel: Int
        get() = R.string.graph_label_speed

    override val xLabel: Int
        get() = R.string.graph_label_distance

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
        summary.deltas.forEach {
            addSegmentToGraphPoints(it, graphPoints)
        }

        val meters = 75F
        return graphPoints
                .inverseSpeed()
                .flattenOutliers()
                .smooth(meters)
    }

    override fun describeYvalue(context: Context, yValue: Float): String {
        val speed = if (inverseSpeed) yValue.inverseSpeed() else yValue
        return statisticsFormatter.convertMeterPerSecondsToSpeed(context, speed, inverseSpeed)
    }

    override fun describeXvalue(context: Context, xValue: Float): String {
        return statisticsFormatter.convertMetersToDistance(context, xValue)
    }

    private fun addSegmentToGraphPoints(deltas: List<Summary.Delta>, graphPoints: MutableList<GraphPoint>) {
        deltas.forEach {
            val speed = it.deltaMeters / (it.deltaMilliseconds / 1000F)
            if (speed >= 0F && it.deltaMeters > 0F) {
                val distance = it.totalMeters - (it.deltaMeters / 2F)
                graphPoints.add(GraphPoint(distance, speed))
            }
        }
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

