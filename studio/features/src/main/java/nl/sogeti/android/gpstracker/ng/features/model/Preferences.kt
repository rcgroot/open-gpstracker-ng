package nl.sogeti.android.gpstracker.ng.features.model

import android.arch.lifecycle.MutableLiveData
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureScope
import javax.inject.Inject

@FeatureScope
class Preferences @Inject constructor() {

    val inverseSpeed = MutableLiveData<Boolean>()
    val wakelockScreen = MutableLiveData<Boolean>()
    val satellite = MutableLiveData<Boolean>()
}

fun MutableLiveData<Boolean>.valueOrFalse(): Boolean = this.value ?: false

fun MutableLiveData<Boolean>.not() =
        this.valueOrFalse().not()
                .also { this.value = it }