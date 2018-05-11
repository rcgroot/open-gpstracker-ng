package nl.sogeti.android.gpstracker.ng.features.graphs

import android.content.Context
import android.support.annotation.WorkerThread
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphValueDescriptor
import nl.sogeti.android.gpstracker.ng.features.model.Preferences
import nl.sogeti.android.gpstracker.ng.features.model.valueOrFalse
import nl.sogeti.android.gpstracker.ng.features.summary.Summary
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class GraphSpeedOVerDistanceDataProvider : GraphValueDescriptor, GraphDataProvider {

    @Inject
    lateinit var statisticsFormatter: StatisticsFormatter

    @Inject
    lateinit var preferences: Preferences

    @Inject
    lateinit var graphSpeedConverter: GraphSpeedConverter

//    private val inverseSpeed
//        get() = preferences.inverseSpeed.valueOrFalse()

    override val yLabel: Int
        get() = R.string.graph_label_speed

    override val xLabel: Int
        get() = R.string.graph_label_distance

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    @WorkerThread
    override fun calculateGraphPoints(summary: Summary): List<GraphPoint> {
        val graphPoints = mutableListOf<GraphPoint>()
        summary.deltas.forEach {
            addSegmentToGraphPoints(it, graphPoints)
        }

//        return graphPoints
//        return filterOutliers(graphPoints)
//        return smoothen(graphPoints)
        return smoothen(filterOutliers(graphPoints))
    }

    override val valueDescriptor: GraphValueDescriptor
        get() = this

    override fun describeYvalue(context: Context, yValue: Float): String {
        return statisticsFormatter.convertMeterPerSecondsToSpeed(context, yValue, preferences.inverseSpeed.valueOrFalse())
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

//
//    fun Float.toY() =
//            if (inverseSpeed) {
//                graphSpeedConverter.speedToYValue(this)
//            } else {
//                this
//            }
//
//    fun Float.toSpeed() =
//            if (inverseSpeed) {
//                graphSpeedConverter.yValueToSpeed(this)
//            } else {
//                this
//            }
}

