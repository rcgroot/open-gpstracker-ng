package nl.sogeti.android.gpstracker.ng.features.graphs

import javax.inject.Inject

class GraphSpeedConverter @Inject constructor() {

    fun speedToYValue(meterPerSecond: Float): Float {
        return (1F / meterPerSecond) / 0.06F
    }

    fun yValueToSpeed(minutePerKilometer: Float): Float {
        val kilometerPerMinute = 1F / minutePerKilometer
        return kilometerPerMinute * 1000 / 60
    }
}
