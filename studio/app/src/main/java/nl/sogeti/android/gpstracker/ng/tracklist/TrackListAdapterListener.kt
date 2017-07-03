package nl.sogeti.android.gpstracker.ng.tracklist

import android.net.Uri

interface TrackListAdapterListener {
    fun didSelectTrack(track: Uri, name: String)
    fun didShareTrack(track: Uri)
    fun didDeleteTrack(track: Uri)
    fun didEditTrack(track: Uri)
    fun didSelectExport()
    fun didSelectImport()
}