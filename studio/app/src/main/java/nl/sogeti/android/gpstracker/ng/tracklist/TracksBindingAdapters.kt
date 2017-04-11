package nl.sogeti.android.gpstracker.ng.tracklist

import android.databinding.BindingAdapter
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import timber.log.Timber

open class TracksBindingAdapters {

    @BindingAdapter("tracks")
    fun setTracks(recyclerView: RecyclerView, tracks: List<Uri>) {
        val viewAdapter: TrackListViewAdapter
        if (recyclerView.adapter is TrackListViewAdapter) {
            viewAdapter = recyclerView.adapter as TrackListViewAdapter
            viewAdapter.model = tracks
        } else {
            viewAdapter = TrackListViewAdapter(recyclerView.context)
            viewAdapter.model = tracks
            recyclerView.adapter = viewAdapter
        }
    }

    @BindingAdapter("tracksListener")
    fun setListener(recyclerView: RecyclerView, listener: TrackListAdapterListener) {
        val adapter = recyclerView.adapter
        if (adapter != null && adapter is TrackListViewAdapter) {
            adapter.listener = listener
        } else {
            Timber.e("Binding listener when missing adapter, are the xml attributes out of order")
        }
    }

    @BindingAdapter("editMode")
    fun setEditMode(card: CardView, editMode: Boolean) {
        if (editMode) {
            card.alpha = 0.5F
        }
        else {
            card.alpha = 1.0F
        }
    }
}