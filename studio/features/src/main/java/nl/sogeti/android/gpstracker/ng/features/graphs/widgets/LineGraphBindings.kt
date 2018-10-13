package nl.sogeti.android.gpstracker.ng.features.graphs.widgets

import androidx.databinding.BindingAdapter
import nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders.GraphDataCalculator

open class LineGraphBindings {

    @BindingAdapter("data", "value_description")
    fun setData(view: LineGraph, data: List<GraphPoint>?, descriptor: GraphDataCalculator?) {
        view.data = data ?: emptyList()
        view.description = descriptor ?: GraphDataCalculator.DefaultGraphValueDescriptor
    }
}
