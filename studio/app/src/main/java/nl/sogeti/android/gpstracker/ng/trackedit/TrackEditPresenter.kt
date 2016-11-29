package nl.sogeti.android.gpstracker.ng.trackedit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.SpinnerAdapter
import android.widget.TextView
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.ContextedPresenter
import nl.sogeti.android.gpstracker.ng.utils.*
import nl.sogeti.android.gpstracker.v2.R

class TrackEditPresenter(val model: TrackEditModel, val listener: Listener) : ContextedPresenter() {

    val spinnerAdapter: SpinnerAdapter by lazy {
        object : BaseAdapter() {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val viewHolder: ViewHolder
                val itemView: View
                if (convertView == null) {
                    itemView = LayoutInflater.from(context).inflate(R.layout.row_track_type, parent, false)
                    viewHolder = ViewHolder(
                            itemView.findViewById(R.id.row_track_type_image) as ImageView,
                            itemView.findViewById(R.id.row_track_type_text) as TextView)
                    itemView.tag = viewHolder
                } else {
                    itemView = convertView
                    viewHolder = convertView.tag as ViewHolder
                }
                val trackType = model.trackTypes[position]
                viewHolder.textView.text = context?.getString(trackType.stringId)
                viewHolder.imageView.setImageDrawable(context?.getDrawable(trackType.drawableId))

                return itemView
            }

            override fun getItem(position: Int): TrackType = model.trackTypes[position]

            override fun getItemId(position: Int): Long = model.trackTypes[position].drawableId.toLong()

            override fun getCount() = model.trackTypes.size
        }
    }

    override fun didStart() {
        val trackUri = model.trackUri.get()
        val trackId: Long = trackUri.lastPathSegment.toLong()

        model.name.set(trackUri?.apply(context!!, { it.getString(ContentConstants.TracksColumns.NAME) ?: "" }))
        val typeSelection = Pair("${ContentConstants.MetaDataColumns.VALUE} = ?", listOf(TrackTypeDescriptions.KEY_META_FIELD_TRACK_TYPE))
        val trackType = trackMetaDataUri(trackId).apply(context!!, { it.getString(ContentConstants.MetaDataColumns.VALUE) }, selectionPair = typeSelection)
        val trackTypeDrawable = TrackTypeDescriptions.convertTypeDescriptionToDrawable(trackType)
        model.selectedPosition.set(model.trackTypes.indexOfFirst { it.drawableId == trackTypeDrawable })
    }

    override fun willStop() {
    }

    fun ok() {
        saveTrackName()
        saveTrackType()
        listener.dismiss()
    }

    fun cancel() {
        listener.dismiss()
    }

    private fun saveTrackType() {
        val trackUri = model.trackUri.get()
        val trackId: Long = trackUri.lastPathSegment.toLong()
        val typeDescription = TrackTypeDescriptions.convertDrawableToTypeDescription(model.trackTypes.get(model.selectedPosition.get()).drawableId)
        trackMetaDataUri(trackId).updateMetaData(context!!, TrackTypeDescriptions.KEY_META_FIELD_TRACK_TYPE, typeDescription)
    }

    private fun saveTrackName() {
        model.trackUri.get()?.updateName(context!!, model.name.get())
    }

    interface Listener {
        fun dismiss()

    }

    data class ViewHolder(val imageView: ImageView, val textView: TextView)
}
