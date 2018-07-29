package nl.sogeti.android.gpstracker.ng.util

import android.support.test.espresso.IdlingResource
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import nl.sogeti.android.gpstracker.ng.base.common.onMainThread
import timber.log.Timber

class IdlingMapResource(map: MapView) : IdlingResource, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnMapLoadedCallback {

    private var isMapLoaded = true
    private var isCameraIdle = true
    private var googleMap: GoogleMap? = null
    private var callback: IdlingResource.ResourceCallback? = null

    init {
        onMainThread {
            map.getMapAsync {
                googleMap = it
                it.setOnCameraIdleListener(this)
                it.setOnCameraMoveListener(this)
                it.setOnMapLoadedCallback(this)
            }
            Timber.d("IdlingMapResource started")
        }
    }

    override fun getName(): String = "MapResource"

    override fun isIdleNow(): Boolean {
        Timber.d("Is idle camera $isCameraIdle && loaded $isMapLoaded")
        return googleMap != null && isCameraIdle && isMapLoaded
    }

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback?) {
        this.callback = callback
    }

    override fun onCameraIdle() {
        Timber.d("onCameraIdle()")
        isCameraIdle = true
        if (isIdleNow) {
            Timber.d("Became idle")
            callback?.onTransitionToIdle()
        }
    }

    override fun onCameraMove() {
        isCameraIdle = false
    }

    override fun onMapLoaded() {
        Timber.d("onMapLoaded()")
        isMapLoaded = true
        if (isIdleNow) {
            Timber.d("Became idle")
            callback?.onTransitionToIdle()
        }
    }
}
