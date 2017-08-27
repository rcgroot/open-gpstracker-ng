@file:Suppress("DEPRECATION")

package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.annotation.SuppressLint
import android.content.Context
import android.location.GpsSatellite
import android.location.GpsStatus
import android.location.LocationManager
import nl.sogeti.android.gpstracker.ng.rules.any
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

@SuppressLint("MissingPermission")
class GpsStatusControllerImplTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    lateinit var sut: GpsStatusControllerImpl
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var listener: GpsStatusController.Listener
    @Mock
    lateinit var locationManager: LocationManager
    @Captor
    lateinit var callbackCapture: ArgumentCaptor<GpsStatus.Listener>

    @Before
    fun setUp() {
        `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager)
        sut = GpsStatusControllerImpl(context, listener)
    }

    @Test
    fun startUpdates() {
        // Act
        sut.startUpdates()
        // Assert
        verify(locationManager).addGpsStatusListener(any())
    }

    @Test
    fun stopUpdates() {
        // Arrange
        sut.startUpdates()
        // Act
        sut.stopUpdates()
        // Assert
        verify(locationManager).removeGpsStatusListener(any())
    }

    @Test
    fun start() {
        // Arrange
        sut.startUpdates()
        verify(locationManager).addGpsStatusListener(callbackCapture.capture())
        val callback = callbackCapture.value
        // Act
        callback.onGpsStatusChanged(GpsStatus.GPS_EVENT_STARTED)
        // Assert
        verify(listener).onStart()
    }

    @Test
    fun stop() {
        // Arrange
        sut.startUpdates()
        verify(locationManager).addGpsStatusListener(callbackCapture.capture())
        val callback = callbackCapture.value
        // Act
        callback.onGpsStatusChanged(GpsStatus.GPS_EVENT_STOPPED)
        // Assert
        verify(listener).onStop()
    }

    @Test
    fun firstFix() {
        // Arrange
        sut.startUpdates()
        verify(locationManager).addGpsStatusListener(callbackCapture.capture())
        val callback = callbackCapture.value
        // Act
        callback.onGpsStatusChanged(GpsStatus.GPS_EVENT_FIRST_FIX)
        // Assert
        verify(listener).onFirstFix()
    }

    @Test
    fun statellites() {
        // Arrange
        sut.startUpdates()
        verify(locationManager).addGpsStatusListener(callbackCapture.capture())
        val callback = callbackCapture.value
        val status = mock(GpsStatus::class.java)
        `when`(locationManager.getGpsStatus(null)).thenReturn(status)
        `when`(status.maxSatellites).thenReturn(3)
        `when`(status.satellites).thenReturn(listOf<GpsSatellite?>(null, null))
        // Act
        callback.onGpsStatusChanged(GpsStatus.GPS_EVENT_SATELLITE_STATUS)
        // Assert
        verify(listener).onChange(2, 3)
    }
}
