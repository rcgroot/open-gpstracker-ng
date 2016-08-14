package nl.sogeti.android.gpstracker.ng.binders

import android.databinding.DataBindingComponent

class MyBindingComponent : DataBindingComponent {
    private val trackAdapter = MyBindingAdapter()

    override fun getMyBindingAdapter(): MyBindingAdapter {
        return trackAdapter
    }
}