package nl.sogeti.android.gpstracker.ng.features.graphs

import javax.inject.Inject

class GraphSpeedConverter @Inject constructor() {

    fun speedToYValue(meterPerSecond: Float): Float {
        if (meterPerSecond < 0.001) {
            return 0F
        }
        return (1F / meterPerSecond) / 0.06F
    }

    fun yValueToSpeed(minutePerKilometer: Float): Float {
        val kilometerPerMinute = 1F / minutePerKilometer
        return kilometerPerMinute * 1000 / 60
    }
}
