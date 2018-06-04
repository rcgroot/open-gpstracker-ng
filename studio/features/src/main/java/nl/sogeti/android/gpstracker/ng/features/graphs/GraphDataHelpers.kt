package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import java.lang.Math.max
import java.lang.Math.sqrt
import kotlin.math.pow

private const val MAX_TIMES_STANDARD_DEVIATION = 2

fun List<GraphPoint>.filterOutliers(): List<GraphPoint> {
    val mean = this.sumByDouble { it.y.toDouble() } / this.size
    val sd = sqrt(this.sumByDouble { (it.y.toDouble() - mean).pow(2) } / this.size)
    val min = max(0.0, mean - sd * MAX_TIMES_STANDARD_DEVIATION)
    val max = mean + sd * MAX_TIMES_STANDARD_DEVIATION
    return this.filter { it.y > min && it.y < max }
}

fun List<GraphPoint>.smoothen(halfSpan: Int) =
        this.mapIndexed { i, point ->
            val ySmooth = localAverage(this, i, halfSpan)
            GraphPoint(point.x, ySmooth.toFloat())
        }

private fun localAverage(points: List<GraphPoint>, pivot: Int, halfSpan: Int): Double {
    val collectedPoints = mutableListOf<GraphPoint>()
    for (i in pivot downTo 0) {
        if (points[pivot].x - points[i].x < halfSpan) {
            collectedPoints.add(points[i])
        } else {
            break
        }
    }
    for (i in pivot + 1 until points.size) {
        if (points[i].x - points[pivot].x < halfSpan) {
            collectedPoints.add(points[i])
        } else {
            break
        }
    }

    return collectedPoints.sumByDouble { it.y.toDouble() } / collectedPoints.size
}
