package nl.sogeti.android.gpstracker.ng.features.graphs.widgets;

import android.databinding.BindingAdapter

open class LineGraphBindings {

    @BindingAdapter("data")
    fun setData(view: LineGraph, data: List<GraphPoint>?) {
        view.data = data ?: emptyList()
    }

    @BindingAdapter("value_description")
    fun setValueDescription(view: LineGraph, descriptor: LineGraph.ValueDescriptor?) {
        view.description = descriptor ?: object : LineGraph.ValueDescriptor {}
    }
}
