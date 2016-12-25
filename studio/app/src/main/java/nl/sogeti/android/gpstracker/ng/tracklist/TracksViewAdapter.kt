package nl.sogeti.android.gpstracker.ng.tracklist

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import nl.sogeti.android.gpstracker.ng.utils.executeOnUiThread
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.RowTrackBinding


class TracksViewAdapter(val model: ObservableArrayList<TrackViewModel>) : RecyclerView.Adapter<TracksViewAdapter.ViewHolder>() {

    var listener: TrackListListener? = null
    private val changeListener: ObservableList.OnListChangedCallback<out ObservableList<TracksViewModel>> = ListObserver()

    init {
        //TODO listen to changes by means of data binding libs
        model.addOnListChangedCallback(changeListener)
    }

    fun destroy() {
        model.removeOnListChangedCallback(changeListener)
    }

    override fun getItemCount(): Int {
        return model.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<RowTrackBinding>(LayoutInflater.from(parent?.context), R.layout.row_track, parent, false)
        val holder = ViewHolder(binding)
        holder.binding.adapter = this
        // Weirdly enough the 'clickable="false"' in the XML resource doesn't work
        holder.binding.rowTrackMap.isClickable = false
        holder.binding.rowTrackMap.onCreate(null)

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val trackViewModel = model[position]
        if (holder != null) {
            holder.binding.viewModel = trackViewModel
            if (holder.googleMap == null) {
                holder.binding.rowTrackMap.getMapAsync {
                    it.uiSettings.isMapToolbarEnabled = false
                    holder.googleMap = it
                    holder.addLinesToMap()
                }
            } else {
                holder.addLinesToMap()
            }
            listener?.willDisplayTrack(trackViewModel, { holder.addLinesToMap() })
        }
    }

    fun didSelectTrack(track: TrackViewModel) {
        listener?.didSelectTrack(track)
    }

    class ViewHolder(val binding: RowTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        var googleMap: GoogleMap? = null

        fun addLinesToMap() {
            googleMap?.clear()
            binding.viewModel.polylines.map {
                googleMap?.addPolyline(it)
            }
        }
    }

    inner class ListObserver : ObservableList.OnListChangedCallback<ObservableList<TracksViewModel>>() {
        override fun onItemRangeInserted(sender: ObservableList<TracksViewModel>?, p1: Int, p2: Int) {
            onChanged(sender)
        }

        override fun onItemRangeChanged(sender: ObservableList<TracksViewModel>?, p1: Int, p2: Int) {
            onChanged(sender)
        }

        override fun onItemRangeMoved(sender: ObservableList<TracksViewModel>?, p1: Int, p2: Int, p3: Int) {
            onChanged(sender)
        }

        override fun onItemRangeRemoved(sender: ObservableList<TracksViewModel>?, p1: Int, p2: Int) {
            onChanged(sender)
        }

        override fun onChanged(sender: ObservableList<TracksViewModel>?) {
            executeOnUiThread { this@TracksViewAdapter.notifyDataSetChanged() }
        }
    }
}

