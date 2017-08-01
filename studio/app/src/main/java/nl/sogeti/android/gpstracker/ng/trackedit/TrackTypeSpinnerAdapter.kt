package nl.sogeti.android.gpstracker.ng.trackedit

import android.content.Context
import android.support.v7.content.res.AppCompatResources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import nl.sogeti.android.gpstracker.v2.R

class TrackTypeSpinnerAdapter(val context: Context, var trackTypes: List<TrackTypeDescriptions.TrackType>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val viewHolder: TrackEditPresenter.ViewHolder
        val itemView: View
        if (convertView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.row_track_type, parent, false)
            viewHolder = TrackEditPresenter.ViewHolder(
                    itemView.findViewById<ImageView>(R.id.row_track_type_image),
                    itemView.findViewById<TextView>(R.id.row_track_type_text))
            itemView.tag = viewHolder
        } else {
            itemView = convertView
            viewHolder = convertView.tag as TrackEditPresenter.ViewHolder
        }
        val trackType = trackTypes[position]
        viewHolder.textView.text = context.getString(trackType.stringId)
        context.let { viewHolder.imageView.setImageDrawable(AppCompatResources.getDrawable(it, trackType.drawableId)) }

        return itemView
    }

    override fun getItem(position: Int): TrackTypeDescriptions.TrackType = trackTypes[position]

    override fun getItemId(position: Int): Long = trackTypes[position].drawableId.toLong()

    override fun getCount() = trackTypes.size
}