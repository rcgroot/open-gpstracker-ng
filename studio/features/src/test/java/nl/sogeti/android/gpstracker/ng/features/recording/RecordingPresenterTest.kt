package nl.sogeti.android.gpstracker.ng.features.recording

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import nl.renedegroot.android.test.utils.any
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.features.model.Preferences
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.excellent
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.high
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.low
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.medium
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingView.SignalQualityLevel.none
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.util.LoggingStateController
import nl.sogeti.android.gpstracker.ng.features.util.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.service.integration.ServiceConstants
import nl.sogeti.android.gpstracker.service.integration.ServiceManager
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatter
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class RecordingPresenterTest {

    lateinit var sut: RecordingPresenter
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var uri: Uri
    @Mock
    lateinit var contentController: ContentController
    @Mock
    lateinit var gpsStatusController: GpsStatusController
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var gpsStatusControllerFactory: GpsStatusControllerFactory
    @Mock
    lateinit var summaryManager: SummaryManager
    @Mock
    lateinit var loggingStateController: LoggingStateController

    @Before
    fun setUp() {
        sut = RecordingPresenter()
        sut.loggingStateController = loggingStateController
        sut.summaryManager = summaryManager
        sut.contentController = contentController
        sut.gpsStatusControllerFactory = gpsStatusControllerFactory
        `when`(gpsStatusControllerFactory.createGpsStatusController(any())).thenReturn(gpsStatusController)
    }

    @Test
    fun testStop() {
        // Arrange
        sut.start()
        loggingStateTo(ServiceConstants.STATE_LOGGING)
        reset(contentController)
        // Act
        sut.stop()
        // Assert
        verify(contentController).unregisterObserver()
        verify(gpsStatusController).stopUpdates()
    }

    @Test
    fun testConnectToLoggingService() {
        // Arrange
        sut.start()
        // Act
        loggingStateTo(ServiceConstants.STATE_LOGGING)
        // Assert
        Assert.assertThat(sut.viewModel.isRecording.get(), `is`(true))
        Assert.assertThat(sut.viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testConnectToPauseService() {
        // Arrange
        sut.start()
        // Act
        loggingStateTo(ServiceConstants.STATE_PAUSED)
        // Assert
        Assert.assertThat(sut.viewModel.isRecording.get(), `is`(true))
        Assert.assertThat(sut.viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testConnectToStoppedService() {
        // Arrange
        sut.start()
        // Act
        loggingStateTo(ServiceConstants.STATE_STOPPED)
        // Assert
        Assert.assertThat(sut.viewModel.isRecording.get(), `is`(false))
        Assert.assertThat(sut.viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testChangeToLoggingService() {
        // Arrange
        sut.start()
        // Act
        loggingStateTo(ServiceConstants.STATE_STOPPED)
        // Assert
        Assert.assertThat(sut.viewModel.isRecording.get(), `is`(false))
        Assert.assertThat(sut.viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testGpsStart() {
        // Act
        sut.onStartListening()
        // Assert
        assertThat(sut.viewModel.isScanning.get(), `is`(true))
        assertThat(sut.viewModel.hasFix.get(), `is`(false))
    }

    @Test
    fun testGpsStop() {
        // Act
        sut.onStopListening()
        // Assert
        assertThat(sut.viewModel.isScanning.get(), `is`(false))
        assertThat(sut.viewModel.hasFix.get(), `is`(false))
        assertThat(sut.viewModel.maxSatellites.get(), `is`(0))
        assertThat(sut.viewModel.currentSatellites.get(), `is`(0))
    }

    @Test
    fun onFirstFix() {
        // Act
        sut.onFirstFix()
        // Assert
        assertThat(sut.viewModel.hasFix.get(), `is`(true))
        assertThat(sut.viewModel.signalQuality.get(), `is`(4))
    }

    @Test
    fun onNoSignal() {
        // Act
        sut.onChange(0, 0)
        // Assert
        assertThat(sut.viewModel.maxSatellites.get(), `is`(0))
        assertThat(sut.viewModel.currentSatellites.get(), `is`(0))
        assertThat(sut.viewModel.signalQuality.get(), `is`(none))
    }

    @Test
    fun onLowSignal() {
        // Act
        sut.onChange(4, 4)
        // Assert
        assertThat(sut.viewModel.maxSatellites.get(), `is`(4))
        assertThat(sut.viewModel.currentSatellites.get(), `is`(4))
        assertThat(sut.viewModel.signalQuality.get(), `is`(low))
    }

    @Test
    fun onMediumSignal() {
        // Act
        sut.onChange(6, 20)
        // Assert
        assertThat(sut.viewModel.maxSatellites.get(), `is`(20))
        assertThat(sut.viewModel.currentSatellites.get(), `is`(6))
        assertThat(sut.viewModel.signalQuality.get(), `is`(medium))
    }

    @Test
    fun onHighSignal() {
        // Act
        sut.onChange(8, 20)
        // Assert
        assertThat(sut.viewModel.signalQuality.get(), `is`(high))
    }

    @Test
    fun onExcellentSignal() {
        // Act
        sut.onChange(10, 20)
        // Assert
        assertThat(sut.viewModel.signalQuality.get(), `is`(excellent))
    }

    private fun loggingStateTo(state: Int) {
        `when`(loggingStateController.loggingState).thenReturn(state)
        `when`(loggingStateController.trackUri).thenReturn(uri)
        sut.didConnectToService(context, state, uri)
    }
}
