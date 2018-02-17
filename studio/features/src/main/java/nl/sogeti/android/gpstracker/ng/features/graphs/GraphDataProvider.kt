package nl.sogeti.android.gpstracker.ng.features.graphs

import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.GraphPoint
import nl.sogeti.android.gpstracker.ng.features.graphs.widgets.LineGraph
import nl.sogeti.android.gpstracker.service.util.Waypoint

interface GraphDataProvider {

    fun calculateGraphPoints(waypoints: List<List<Waypoint>>): List<GraphPoint>
    val valueDescriptor: LineGraph.ValueDescriptor
}