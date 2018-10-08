package nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders

import android.content.Context
import androidx.annotation.WorkerThread
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.summary.Summary

interface GraphDataCalculator {

    @WorkerThread
    fun calculateGraphPoints(summary: Summary): List<GraphPoint>

    val xLabel: Int
    val yLabel: Int

    fun describeXvalue(context: Context, xValue: Float): String {
        return ""
    }

    fun describeYvalue(context: Context, yValue: Float): String {
        return ""
    }

    fun prettyMinYValue(context: Context, yValue: Float): Float

    fun prettyMaxYValue(context: Context, yValue: Float): Float

    object DefaultGraphValueDescriptor : GraphDataCalculator {
        override fun calculateGraphPoints(summary: Summary) = emptyList<GraphPoint>()

        override fun prettyMinYValue(context: Context, yValue: Float) = yValue

        override fun prettyMaxYValue(context: Context, yValue: Float) = yValue

        override val xLabel: Int
            get() = 0
        override val yLabel: Int
            get() = 1
    }
}
