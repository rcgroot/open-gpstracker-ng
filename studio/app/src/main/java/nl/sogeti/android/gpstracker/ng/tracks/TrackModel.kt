package nl.sogeti.android.gpstracker.ng.tracks

import android.databinding.BaseObservable
import android.databinding.Bindable
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.graphics.Bitmap
import nl.sogeti.android.gpstracker.v2.BR
import nl.sogeti.android.gpstracker.v2.R

class TrackModel : BaseObservable() {

    val iconType = ObservableInt(R.drawable.ic_track_type_default_24dp);
    /**
     * Just to try out the Bindable annotation
     */
    @Bindable
    var name = ""
        set(value) {
            field = value
            notifyPropertyChanged(BR.name)
        }
    val startDay = ObservableField<String>("--")
    val duration = ObservableField<String>("--")
    val distance = ObservableField<String>("--")
    val overviewMap = ObservableField<Bitmap>()
}