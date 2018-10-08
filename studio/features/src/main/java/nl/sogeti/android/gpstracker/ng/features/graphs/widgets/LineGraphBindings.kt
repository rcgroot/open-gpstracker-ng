package nl.sogeti.android.gpstracker.ng.features.graphs.widgets;

import androidx.databinding.BindingAdapter
import nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders.GraphDataCalculator

open class LineGraphBindings {

    @BindingAdapter("data")
    fun setData(view: LineGraph, data: List<GraphPoint>?) {
        view.data = data ?: emptyList()
    }

    @BindingAdapter("value_description")
    fun setValueDescription(view: LineGraph, descriptor: GraphDataCalculator?) {
        view.description = descriptor ?: GraphDataCalculator.DefaultGraphValueDescriptor
    }
}
