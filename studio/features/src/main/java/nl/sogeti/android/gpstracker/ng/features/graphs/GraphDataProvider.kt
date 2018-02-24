package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.LineGraph
import nl.sogeti.android.gpstracker.ng.features.summary.Summary
import nl.sogeti.android.gpstracker.service.util.Waypoint

interface GraphDataProvider {

    fun calculateGraphPoints(summary: Summary): List<GraphPoint>
    val valueDescriptor: LineGraph.ValueDescriptor
    val xLabel: Int
    val yLabel: Int
}
