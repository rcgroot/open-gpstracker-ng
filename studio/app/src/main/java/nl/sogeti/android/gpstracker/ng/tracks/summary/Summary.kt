package nl.sogeti.android.gpstracker.ng.tracks.summary

import android.net.Uri

data class Summary(val track: Uri,
                   val name: String,
                   val type: Int,
                   val start: String,
                   val duration: String,
                   val distance: String,
                   val timestamp: Long)