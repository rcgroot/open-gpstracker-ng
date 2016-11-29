package nl.sogeti.android.gpstracker.ng.trackedit

import android.content.Context
import nl.sogeti.android.gpstracker.v2.R

class TrackTypeDescriptions(val context: Context) {

    companion object {
        val KEY_META_FIELD_TRACK_TYPE = "SUMMARY_TYPE"
        val VALUE_TYPE_DEFAULT = "TYPE_DEFAULT"
        val VALUE_TYPE_BIKE = "TYPE_BIKE"
        val VALUE_TYPE_BOAT = "TYPE_BOAT"
        val VALUE_TYPE_CAR = "TYPE_CAR"
        val VALUE_TYPE_RUN = "TYPE_RUN"
        val VALUE_TYPE_WALK = "TYPE_WALK"
        val VALUE_TYPE_TRAIN = "TYPE_TRAIN"

        val allTrackTypes by lazy {
            listOf(
                    TrackType(R.drawable.ic_track_type_default, R.string.track_type_default),
                    TrackType(R.drawable.ic_track_type_walk, R.string.track_type_walk),
                    TrackType(R.drawable.ic_track_type_run, R.string.track_type_run),
                    TrackType(R.drawable.ic_track_type_bike, R.string.track_type_bike),
                    TrackType(R.drawable.ic_track_type_car, R.string.track_type_car),
                    TrackType(R.drawable.ic_track_type_train, R.string.track_type_train),
                    TrackType(R.drawable.ic_track_type_boat, R.string.track_type_boat)
            )
        }

        fun convertTypeDescriptionToDrawable(description: String?): Int =
                when (description) {
                    KEY_META_FIELD_TRACK_TYPE -> R.drawable.ic_track_type_default
                    VALUE_TYPE_BIKE -> R.drawable.ic_track_type_bike
                    VALUE_TYPE_BOAT -> R.drawable.ic_track_type_boat
                    VALUE_TYPE_CAR -> R.drawable.ic_track_type_car
                    VALUE_TYPE_RUN -> R.drawable.ic_track_type_run
                    VALUE_TYPE_WALK -> R.drawable.ic_track_type_walk
                    VALUE_TYPE_TRAIN -> R.drawable.ic_track_type_train
                    else -> R.drawable.ic_track_type_default
                }


        fun convertDrawableToTypeDescription(drawable: Int?): String =
                when (drawable) {
                    R.drawable.ic_track_type_default -> VALUE_TYPE_DEFAULT
                    R.drawable.ic_track_type_bike -> VALUE_TYPE_BIKE
                    R.drawable.ic_track_type_boat -> VALUE_TYPE_BOAT
                    R.drawable.ic_track_type_car -> VALUE_TYPE_CAR
                    R.drawable.ic_track_type_run -> VALUE_TYPE_RUN
                    R.drawable.ic_track_type_walk -> VALUE_TYPE_WALK
                    R.drawable.ic_track_type_train -> VALUE_TYPE_TRAIN
                    else -> VALUE_TYPE_DEFAULT
                }


    }
}

data class TrackType(val drawableId: Int, val stringId: Int)