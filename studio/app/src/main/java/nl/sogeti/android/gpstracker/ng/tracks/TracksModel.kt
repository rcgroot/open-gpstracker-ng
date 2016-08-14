package nl.sogeti.android.gpstracker.ng.tracks

import android.databinding.ObservableArrayList


class TracksModel {
    val track = ObservableArrayList<TrackModel>()
    init {

        for (i in 1..5) {
            var model = TrackModel()
            model.name = "Track $i"
            track.add(model)
        }
    }
}