package nl.sogeti.android.gpstracker.ng.graphs

import android.databinding.ObservableField
import android.net.Uri

class GraphsViewModel(uri: Uri) {
    val trackUri = ObservableField<Uri>(uri)
}
