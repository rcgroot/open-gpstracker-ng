package nl.sogeti.android.gpstracker.ng.tracks

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.RowTrackBinding

class TracksAdapter(val model: TracksModel) : RecyclerView.Adapter<TracksAdapter.ViewHolder>() {
    override fun getItemCount(): Int {
        return model.track.size
    }

    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val trackModel = model.track[position]
        if (holder != null) {
            val binding = holder.binding
            binding.model = trackModel
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<RowTrackBinding>(LayoutInflater.from(parent?.context), R.layout.row_track, parent, false)
        val viewHolder = ViewHolder(binding)

        return viewHolder
    }

    class ViewHolder(val binding: RowTrackBinding) : RecyclerView.ViewHolder(binding.root) {
    }
}