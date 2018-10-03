package nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders

import android.content.Context
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphSpeedConverter
import nl.sogeti.android.gpstracker.v2.sharedwear.util.getFloat
import javax.inject.Inject

class SpeedRangePicker(private val inverseSpeed: Boolean) {

    @Inject
    lateinit var graphSpeedConverter: GraphSpeedConverter

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    fun prettyMinYValue(context: Context, yValue: Float): Float {
        val minSpeed: Float
        minSpeed = if (inverseSpeed) {
            graphSpeedConverter.speedToYValue(yValue.toInt().toFloat())
        } else {
            val conversion = context.resources.getFloat(nl.sogeti.android.gpstracker.v2.sharedwear.R.string.mps_to_speed)
            val speed = yValue * conversion
            (10 * (speed.toInt() / 10)) / conversion
        }

        return minSpeed
    }

    fun prettyMaxYValue(context: Context, yValue: Float): Float {
        val minSpeed: Float
        minSpeed = if (inverseSpeed) {
            graphSpeedConverter.speedToYValue((1 + yValue.toInt()).toFloat())
        } else {
            val conversion = context.resources.getFloat(nl.sogeti.android.gpstracker.v2.sharedwear.R.string.mps_to_speed)
            val speed = yValue * conversion
            (10 * (1 + speed.toInt() / 10)) / conversion
        }

        return minSpeed
    }
}
