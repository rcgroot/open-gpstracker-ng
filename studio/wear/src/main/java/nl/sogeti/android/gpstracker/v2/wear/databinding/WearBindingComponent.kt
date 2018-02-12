package nl.sogeti.android.gpstracker.v2.wear.databinding

import android.databinding.DataBindingComponent

open class WearBindingComponent : DataBindingComponent {

    override fun getWearBindingAdapters(): WearBindingAdapters = WearBindingAdapters()
}
