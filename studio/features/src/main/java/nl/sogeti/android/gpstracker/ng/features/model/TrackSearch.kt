package nl.sogeti.android.gpstracker.ng.features.model

import android.arch.lifecycle.MutableLiveData
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureScope
import javax.inject.Inject

@FeatureScope
class TrackSearch @Inject constructor() {

    val query: MutableLiveData<String> = MutableLiveData()
}
