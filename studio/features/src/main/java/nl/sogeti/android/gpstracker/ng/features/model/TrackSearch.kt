package nl.sogeti.android.gpstracker.ng.features.model

import androidx.lifecycle.MutableLiveData
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureScope
import javax.inject.Inject

@FeatureScope
class TrackSearch @Inject constructor() {

    val query: MutableLiveData<String> = MutableLiveData()
}
