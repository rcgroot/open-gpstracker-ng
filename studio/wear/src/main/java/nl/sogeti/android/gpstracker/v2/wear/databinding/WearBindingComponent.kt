package nl.sogeti.android.gpstracker.v2.wear.databinding

import androidx.databinding.DataBindingComponent

open class WearBindingComponent : DataBindingComponent {

    override fun getWearBindingAdapters(): WearBindingAdapters = WearBindingAdapters()
}
