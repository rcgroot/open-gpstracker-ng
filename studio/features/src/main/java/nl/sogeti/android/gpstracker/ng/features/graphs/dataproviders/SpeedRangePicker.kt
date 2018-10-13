package nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders

import android.content.Context
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphSpeedConverter
import nl.sogeti.android.gpstracker.v2.sharedwear.R
import nl.sogeti.android.gpstracker.v2.sharedwear.util.getFloat
import javax.inject.Inject

class SpeedRangePicker(private val inverseSpeed: Boolean) {

    @Inject
    lateinit var graphSpeedConverter: GraphSpeedConverter

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    fun prettyMinYValue(context: Context, yValue: Float): Float =
            if (inverseSpeed) {
                lowerBoundForInverseValue(yValue)
            } else {
                lowerBoundForValue(context, yValue)
            }

    fun prettyMaxYValue(context: Context, yValue: Float) =
            if (inverseSpeed) {
                upperBoundForInverseValue(yValue)
            } else {
                upperBoundForValue(context, yValue)
            }

    private fun lowerBoundForValue(context: Context, yValue: Float): Float {
        val conversion = context.resources.getFloat(nl.sogeti.android.gpstracker.v2.sharedwear.R.string.mps_to_speed)
        val speed = yValue * conversion
        return (10 * (speed.toInt() / 10)) / conversion
    }

    private fun upperBoundForValue(context: Context, yValue: Float): Float {
        val conversion = context.resources.getFloat(R.string.mps_to_speed)
        val speed = yValue * conversion
        return (10 * (1 + speed.toInt() / 10)) / conversion
    }

    private fun lowerBoundForInverseValue(yValue: Float): Float {
        return yValue.toInt().toFloat()
    }

    private fun upperBoundForInverseValue(yValue: Float) =
            (1 + yValue.toInt()).toFloat()

}
