package nl.sogeti.android.gpstracker.ng.features.model

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.support.annotation.WorkerThread
import androidx.content.edit
import nl.sogeti.android.gpstracker.ng.base.dagger.DiskIO
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureScope
import java.util.concurrent.Executor
import javax.inject.Inject

@FeatureScope
class Preferences @Inject constructor(context: Context, @DiskIO private val executor: Executor) {

    val inverseSpeed = MutableLiveData<Boolean>()
    val wakelockScreen = MutableLiveData<Boolean>()
    val satellite = MutableLiveData<Boolean>()
    private val preferences = context.getSharedPreferences("settings", MODE_PRIVATE)
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            INVERSE_SPEED -> executor.execute { readInverseSpeed() }
            WAKE_LOCK_SCREEN -> executor.execute { readWakeLockScreen() }
            SATELLITE -> executor.execute { readSatellite() }
        }
    }
    private val inverseSpeedObserver = Observer<Boolean> { _ ->
        inverseSpeed.storeAsPreference(INVERSE_SPEED)
    }
    private val wakelockScreenObserver = Observer<Boolean> { _ ->
        wakelockScreen.storeAsPreference(WAKE_LOCK_SCREEN)
    }
    private val satelliteObserver = Observer<Boolean> { _ ->
        satellite.storeAsPreference(SATELLITE)
    }

    init {
        preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        inverseSpeed.observeForever(inverseSpeedObserver)
        wakelockScreen.observeForever(wakelockScreenObserver)
        satellite.observeForever(satelliteObserver)

        executor.execute {
            readInverseSpeed()
            readWakeLockScreen()
            readSatellite()
        }
    }

    @WorkerThread
    private fun readSatellite() {
        satellite.postValue(preferences.getBoolean(SATELLITE, false))
    }

    @WorkerThread
    private fun readWakeLockScreen() {
        wakelockScreen.postValue(preferences.getBoolean(WAKE_LOCK_SCREEN, false))
    }

    @WorkerThread
    private fun readInverseSpeed() {
        inverseSpeed.postValue(preferences.getBoolean(INVERSE_SPEED, false))
    }

    fun MutableLiveData<Boolean>.storeAsPreference(key: String) {
        val storedValue = preferences.getBoolean(key, false)
        val value = valueOrFalse()
        if (storedValue != value) {
            preferences.edit {
                putBoolean(key, value)
            }
        }
    }

    companion object {
        private const val INVERSE_SPEED = "INVERSE_SPEED"
        private const val WAKE_LOCK_SCREEN = "WAKE_LOCK_SCREEN"
        private const val SATELLITE = "SATELLITE"
    }
}

fun MutableLiveData<Boolean>.valueOrFalse(): Boolean = this.value ?: false

fun MutableLiveData<Boolean>.not() =
        this.valueOrFalse().not()
                .also { this.value = it }