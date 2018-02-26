package nl.sogeti.android.gpstracker.ng.features.trackedit

import android.databinding.BindingAdapter
import android.support.v7.widget.AppCompatSpinner
import nl.sogeti.android.gpstracker.ng.features.databinding.CommonBindingAdapters

class TrackTypesBindingAdapters : CommonBindingAdapters() {

    @BindingAdapter("trackTypes")
    fun setTrackTypes(spinner: AppCompatSpinner, trackTypes: List<TrackTypeDescriptions.TrackType>) {
        val viewAdapter: TrackTypeSpinnerAdapter
        if (spinner.adapter is TrackTypeSpinnerAdapter) {
            viewAdapter = spinner.adapter as TrackTypeSpinnerAdapter
            viewAdapter.trackTypes = trackTypes
        } else {
            viewAdapter = TrackTypeSpinnerAdapter(spinner.context, trackTypes)
            spinner.adapter = viewAdapter
            val tag = spinner.tag
            if (tag is Int) {
                spinner.setSelection(tag)
            }
        }
    }

    @BindingAdapter("selection")
    fun setSelected(spinner: AppCompatSpinner, selection: Int) {
        if (spinner.adapter != null && spinner.selectedItemPosition != selection) {
            spinner.setSelection(selection)
        } else {
            spinner.tag = selection
        }
    }
}
