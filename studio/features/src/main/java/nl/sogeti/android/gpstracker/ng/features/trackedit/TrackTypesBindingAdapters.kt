package nl.sogeti.android.gpstracker.ng.features.trackedit

import androidx.databinding.BindingAdapter
import androidx.appcompat.widget.AppCompatSpinner
import nl.sogeti.android.gpstracker.ng.features.databinding.CommonBindingAdapters

class TrackTypesBindingAdapters : CommonBindingAdapters() {

    @BindingAdapter("trackTypes", "selection", requireAll = false)
    fun setTrackTypes(spinner: AppCompatSpinner,
                      trackTypes: List<TrackTypeDescriptions.TrackType>?,
                      selection: Int?) {
        val viewAdapter: TrackTypeSpinnerAdapter
        if (spinner.adapter is TrackTypeSpinnerAdapter) {
            viewAdapter = spinner.adapter as TrackTypeSpinnerAdapter
            viewAdapter.trackTypes = trackTypes ?: listOf()
        } else {
            viewAdapter = TrackTypeSpinnerAdapter(spinner.context, trackTypes ?: listOf())
            spinner.adapter = viewAdapter
        }

        if (selection != null) {
            spinner.setSelection(selection)
        } else {
            spinner.setSelection(AppCompatSpinner.INVALID_POSITION)
        }
    }
}
