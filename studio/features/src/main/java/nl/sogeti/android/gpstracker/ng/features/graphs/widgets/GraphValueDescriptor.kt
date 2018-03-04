package nl.sogeti.android.gpstracker.ng.features.graphs.widgets

import android.content.Context

interface GraphValueDescriptor {
    fun describeXvalue(context: Context, xValue: Float): String {
        return ""
    }

    fun describeYvalue(context: Context, yValue: Float): String {
        return ""
    }
}