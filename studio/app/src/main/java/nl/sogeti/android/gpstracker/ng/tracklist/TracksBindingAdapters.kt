package nl.sogeti.android.gpstracker.ng.tracklist

import android.databinding.BindingAdapter
import android.databinding.ObservableArrayList
import android.net.Uri
import android.support.v7.widget.RecyclerView
import timber.log.Timber

open class TracksBindingAdapters {

    @BindingAdapter("tracks")
    fun setTracks(recyclerView: RecyclerView, tracks: ObservableArrayList<Uri>) {
        val viewAdapter: TracksViewAdapter
        if (recyclerView.adapter is TracksViewAdapter) {
            viewAdapter = recyclerView.adapter as TracksViewAdapter
            viewAdapter.model = tracks
        } else {
            viewAdapter = TracksViewAdapter(recyclerView.context)
            viewAdapter.model = tracks
            recyclerView.adapter = viewAdapter
        }
    }

    @BindingAdapter("tracksListener")
    fun setListener(recyclerView: RecyclerView, listener: TrackListListener) {
        val adapter = recyclerView.adapter
        if (adapter != null && adapter is TracksViewAdapter) {
            adapter.listener = listener
        } else {
            Timber.e("Binding listener when missing adapter, are the xml attributes out of order")
        }
    }
}