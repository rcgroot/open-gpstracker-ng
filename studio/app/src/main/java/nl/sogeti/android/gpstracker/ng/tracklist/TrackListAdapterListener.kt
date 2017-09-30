package nl.sogeti.android.gpstracker.ng.tracklist

import android.net.Uri

interface TrackListAdapterListener {
    fun didSelectTrack(track: Uri, name: String)
    fun didDeleteTrack(track: Uri)
    fun didEditTrack(track: Uri)
    fun didSelectExportToDirectory()
    fun didSelectImportFromDirectory()
    fun didSelectExportTrack(track: Uri)
    fun didSelectImportTrack()
}
