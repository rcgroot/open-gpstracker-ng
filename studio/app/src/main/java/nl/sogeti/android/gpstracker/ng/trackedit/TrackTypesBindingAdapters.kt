package nl.sogeti.android.gpstracker.ng.trackedit

import android.databinding.BindingAdapter
import android.support.v7.widget.AppCompatSpinner

class TrackTypesBindingAdapters {

    @BindingAdapter("trackTypes")
    fun setTracks(spinner: AppCompatSpinner, trackTypes: List<TrackTypeDescriptions.TrackType>) {
        val viewAdapter: TrackTypeSpinnerAdapter
        if (spinner.adapter is TrackTypeSpinnerAdapter) {
            viewAdapter = spinner.adapter as TrackTypeSpinnerAdapter
            viewAdapter.trackTypes = trackTypes
        } else {
            viewAdapter = TrackTypeSpinnerAdapter(spinner.context, trackTypes)
            spinner.adapter = viewAdapter
        }
    }
}