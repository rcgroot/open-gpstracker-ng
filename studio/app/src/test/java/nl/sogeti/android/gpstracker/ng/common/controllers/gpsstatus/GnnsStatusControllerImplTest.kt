package nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus

import android.annotation.SuppressLint
import android.content.Context
import android.location.GnssStatus
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

@SuppressLint("NewApi")
class GnnsStatusControllerImplTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    lateinit var sut: GnnsStatusControllerImpl
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var locationManager: LocationManager
    @Mock
    lateinit var listener: GpsStatusController.Listener
    @Captor
    lateinit var callbackCapture: ArgumentCaptor<GnssStatus.Callback>

    @Before
    fun setUp() {
        `when`(context.getSystemService(Context.LOCATION_SERVICE)).thenReturn(locationManager)
        sut = GnnsStatusControllerImpl(context, listener)
    }

    @Test
    fun startUpdates() {
        // Act
        sut.startUpdates()
        // Assert
        verify(locationManager).registerGnssStatusCallback(any())
    }

    @Test
    fun stopUpdates() {
        // Arrange
        sut.startUpdates()
        // Act
        sut.stopUpdates()
        // Assert
        verify(locationManager).unregisterGnssStatusCallback(any())
    }

    @Test
    fun start() {
        // Arrange
        sut.startUpdates()
        verify(locationManager).registerGnssStatusCallback(callbackCapture.capture())
        val callback = callbackCapture.value
        // Act
        callback.onStarted()
        // Assert
        verify(listener).onStart()
    }

    @Test
    fun stop() {
        // Arrange
        sut.startUpdates()
        verify(locationManager).registerGnssStatusCallback(callbackCapture.capture())
        val callback = callbackCapture.value
        // Act
        callback.onStopped()
        // Assert
        verify(listener).onStop()
    }

    @Test
    fun firstFix() {
        // Arrange
        sut.startUpdates()
        verify(locationManager).registerGnssStatusCallback(callbackCapture.capture())
        val callback = callbackCapture.value
        // Act
        callback.onFirstFix(123)
        // Assert
        verify(listener).onFirstFix()
    }

    @Test
    fun statellites() {
        // Arrange
        sut.startUpdates()
        verify(locationManager).registerGnssStatusCallback(callbackCapture.capture())
        val callback = callbackCapture.value
        val status = mock(GnssStatus::class.java)
        `when`(status.satelliteCount).thenReturn(3)
        `when`(status.usedInFix(0)).thenReturn(true)
        `when`(status.usedInFix(2)).thenReturn(true)
        // Act
        callback.onSatelliteStatusChanged(status)
        // Assert
        verify(listener).onChange(2, 3)
    }
}