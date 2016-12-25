package nl.sogeti.android.gpstracker.ng.tracklist

interface TrackListListener {
    fun willDisplayTrack(track: TrackViewModel, completion: () -> Unit)
    fun didSelectTrack(track: TrackViewModel)
}