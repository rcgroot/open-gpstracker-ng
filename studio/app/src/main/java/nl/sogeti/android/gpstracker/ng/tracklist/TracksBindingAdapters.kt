package nl.sogeti.android.gpstracker.ng.tracklist

import android.databinding.BindingAdapter
import android.net.Uri
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.View.GONE
import android.view.View.VISIBLE
import nl.sogeti.android.gpstracker.v2.R
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
        val share = card.findViewById(R.id.row_track_share)
        val delete = card.findViewById(R.id.row_track_delete)
        val edit = card.findViewById(R.id.row_track_edit)
        if (editMode) {
            if (share.visibility != VISIBLE) {
                share.alpha = 0F
                edit.alpha = 0F
                delete.alpha = 0F
            }
            share.visibility = VISIBLE
            edit.visibility = VISIBLE
            delete.visibility = VISIBLE
            share.animate().alpha(1.0F)
            edit.animate().alpha(1.0F)
            delete.animate().alpha(1.0F)
        } else if (share.visibility == VISIBLE) {
            share.animate().alpha(0.0F)
            edit.animate().alpha(0.0F)
            delete.animate().alpha(0.0F).withEndAction {
                share.visibility = GONE
                edit.visibility = GONE
                delete.visibility = GONE
            }
        }
    }
}