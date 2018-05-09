package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import kotlin.math.pow
import kotlin.math.sqrt

private const val halfSpan = 12
private const val maxTimeDeviation = 2

fun smoothen(points: List<GraphPoint>) =
        points.mapIndexed { i, point ->
            val n = listOf(halfSpan, i, points.size - 1 - i).min() ?: 0
            val count = n * 2 + 1
            val ySmooth = points.subList(i - n, i + 1 + n).sumByDouble { it.y.toDouble() } / count
            GraphPoint(point.x, ySmooth.toFloat())
        }

fun filterOutliers(points: List<GraphPoint>): List<GraphPoint> {
    val mean = points.sumByDouble { it.y.toDouble() } / points.size
    val sd = sqrt(points.sumByDouble { (it.y.toDouble() - mean).pow(2) } / (points.size - 1))
    val min = mean - maxTimeDeviation * sd
    val max = mean + maxTimeDeviation * sd
    return points.filter { it.y > min && it.y < max }
}
