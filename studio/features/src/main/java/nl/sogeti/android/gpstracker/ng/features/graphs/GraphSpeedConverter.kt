package nl.sogeti.android.gpstracker.ng.features.graphs

import timber.log.Timber
import javax.inject.Inject
import kotlin.math.pow

class GraphSpeedConverter @Inject constructor() {

    var maxMeterPerSecond = TOO_FAST
    var minMeterPerSecond = TOO_SLOW

    fun speedToYValue(meterPerSecond: Float): Float {
        return when {
            meterPerSecond < minMeterPerSecond -> minMeterPerSecond
            meterPerSecond > maxMeterPerSecond -> maxMeterPerSecond
            else -> {
                (1F / meterPerSecond) / 0.06F
            }
        }
    }

    fun yValueToSpeed(minutePerKilometer: Float): Float {
        val kilometerPerMinute = 1F / minutePerKilometer
        val meterPerSecond = kilometerPerMinute * 1000 / 60
        return meterPerSecond
    }

    companion object {
        private const val TOO_FAST = 6F
        private const val TOO_SLOW = 0.5F
    }

    fun calculateMinMax(speeds: List<Pair<Float, Float>>) {
        val selectedSpeeds = speeds.filter { it.first > 0F && it.second > 0.01F }
        val distance = selectedSpeeds.fold(0F, { acc, (meter, _) -> acc + meter })
        val duration = selectedSpeeds.fold(0F, { acc, (_, seconds) -> acc + seconds })
        val average = distance / duration

        val standardDeviation = (selectedSpeeds.fold(0F, { acc, fl -> acc + (fl.first - average).pow(2) }) / speeds.size).pow(0.5F)
        Timber.d("Average $distance/$duration = $average with variance $standardDeviation")
        minMeterPerSecond = average * 0.5F
        maxMeterPerSecond = average * 1.5F
    }
}
