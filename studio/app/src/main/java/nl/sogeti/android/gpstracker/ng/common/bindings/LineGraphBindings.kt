package nl.sogeti.android.gpstracker.ng.common.bindings;

import android.databinding.BindingAdapter;
import nl.sogeti.android.widgets.model.GraphPoint
import nl.sogeti.android.widgets.LineGraph

class LineGraphBindings {

    @BindingAdapter("data")
    fun setData(view: LineGraph, data: List<GraphPoint>?) {
        view.data = data ?: emptyList()
    }

    @BindingAdapter("value_description")
    fun setValueDescription(view: LineGraph, descriptor: LineGraph.ValueDescriptor?) {
        view.description = descriptor ?: object : LineGraph.ValueDescriptor {}
    }
}
