package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import java.lang.Math.max
import java.lang.Math.sqrt
import kotlin.math.pow

private const val HALF_SPAN_DEFAULT = 6
private const val MAX_TIMES_STANDARD_DEVIATION = 2

fun List<GraphPoint>.filterOutliers(): List<GraphPoint> {
    val mean = this.sumByDouble { it.y.toDouble() } / this.size
    val sd = sqrt(this.sumByDouble { (it.y.toDouble() - mean).pow(2) } / (this.size - 1))
    val min = max(0.0, mean - sd * MAX_TIMES_STANDARD_DEVIATION)
    val max = mean + sd * MAX_TIMES_STANDARD_DEVIATION
    return this.filter { it.y > min && it.y < max }
}

fun List<GraphPoint>.smoothen() =
        this.mapIndexed { i, point ->
            val ySmooth = localAverage(this, i)
            GraphPoint(point.x, ySmooth.toFloat())
        }

private fun localAverage(points: List<GraphPoint>, i: Int, halfSpan: Int = HALF_SPAN_DEFAULT): Double {
    val n = listOf(halfSpan, i, points.size - 1 - i).min() ?: 0
    val count = n * 2 + 1
    return points.subList(i - n, i + 1 + n).sumByDouble { it.y.toDouble() } / count
}
