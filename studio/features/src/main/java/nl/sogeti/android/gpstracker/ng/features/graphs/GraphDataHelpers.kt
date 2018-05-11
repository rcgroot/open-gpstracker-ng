package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import kotlin.math.pow
import kotlin.math.sqrt

private const val HALF_SPAN_DEFAULT = 6
private const val MAX_TIMES_STANDARD_DEVIATION = 3

fun smoothen(points: List<GraphPoint>) =
        points.mapIndexed { i, point ->
            val ySmooth = localAverage(points, i)
            GraphPoint(point.x, ySmooth.toFloat())
        }

fun filterOutliers(points: List<GraphPoint>): List<GraphPoint> {
    val mean = points.sumByDouble { it.y.toDouble() } / points.size
    val sd = sqrt(points.sumByDouble { (it.y.toDouble() - mean).pow(2) } / (points.size - 1))
    val min = mean - MAX_TIMES_STANDARD_DEVIATION * sd
    val max = mean + MAX_TIMES_STANDARD_DEVIATION * sd
    return points.mapIndexed { i, point ->
        if (point.y > min && point.y < max) {
            point
        } else {
            GraphPoint(point.x, localAverage(points, i, 3).toFloat())
        }
    }
}

private fun localAverage(points: List<GraphPoint>, i: Int, halfSpan: Int = HALF_SPAN_DEFAULT): Double {
    val n = listOf(halfSpan, i, points.size - 1 - i).min() ?: 0
    val count = n * 2 + 1
    return points.subList(i - n, i + 1 + n).sumByDouble { it.y.toDouble() } / count
}
