package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphValueDescriptor
import nl.sogeti.android.gpstracker.ng.features.summary.Summary

interface GraphDataProvider {

    fun calculateGraphPoints(summary: Summary): List<GraphPoint>
    val valueDescriptor: GraphValueDescriptor
    val xLabel: Int
    val yLabel: Int
}
