package nl.sogeti.android.gpstracker.ng.trackedit

import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.net.Uri

class TrackEditModel {
    val trackUri = ObservableField<Uri>()
    val name = ObservableField<String>()
    val selectedPosition = ObservableInt()
    val trackTypes = TrackTypeDescriptions.allTrackTypes
}
