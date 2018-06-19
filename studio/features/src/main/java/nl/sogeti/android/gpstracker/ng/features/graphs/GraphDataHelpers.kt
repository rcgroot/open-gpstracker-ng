package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import java.lang.Math.max
import java.lang.Math.sqrt
import kotlin.math.pow

private const val MAX_TIMES_STANDARD_DEVIATION = 2

fun List<GraphPoint>.flattenOutliers(): List<GraphPoint> {
    val mean = this.sumByDouble { it.y.toDouble() } / this.size
    val sd = sqrt(this.sumByDouble { (it.y.toDouble() - mean).pow(2) } / this.size)
    val min = max(0.0, mean - sd * MAX_TIMES_STANDARD_DEVIATION)
    val max = mean + sd * MAX_TIMES_STANDARD_DEVIATION
    return this.mapIndexed { i, point ->
        if (point.y > min && point.y < max) {
            point
        } else {
            this.neighboursAverage(i)
        }
    }
}

fun List<GraphPoint>.smoothen(span: Float) =
        this.mapIndexed { i, point ->
            val ySmooth = this.localAverage(i, span / 2F)
            GraphPoint(point.x, ySmooth.toFloat())
        }

fun <T> List<T>.condens(together: (T, T) -> Boolean, transform: (List<T>) -> T): List<T> {
    val result = mutableListOf<T>()
    var rangeStart = 0
    forEachIndexed { index, item ->
        if (!together(this[rangeStart], item)) {
            result.add(transform(subList(rangeStart, index)))
            rangeStart = index
        }
    }
    val tail = subList(rangeStart, size)
    if (tail.isNotEmpty()) {
        result.add(transform(tail))
    }
    return result
}

private fun List<GraphPoint>.neighboursAverage(pivot: Int): GraphPoint =
        when (pivot) {
            0 -> this[1]
            this.lastIndex -> this[pivot - 1]
            else -> {
                val average = (this[pivot - 1].y + this[pivot + 1].y) / 2F
                GraphPoint(this[pivot].x, average)
            }
        }

private fun List<GraphPoint>.localAverage(pivot: Int, halfSpan: Float): Double {
    val collectedPoints = mutableListOf<GraphPoint>()
    for (i in pivot downTo 0) {
        if (this[pivot].x - this[i].x < halfSpan) {
            collectedPoints.add(this[i])
        } else {
            break
        }
    }
    for (i in pivot + 1 until this.size) {
        if (this[i].x - this[pivot].x < halfSpan) {
            collectedPoints.add(this[i])
        } else {
            break
        }
    }

    return collectedPoints.sumByDouble { it.y.toDouble() } / collectedPoints.size
}
