package nl.sogeti.android.gpstracker.ng.recording

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceConstants
import nl.sogeti.android.gpstracker.integration.ServiceManager
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.excellent
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.high
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.low
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.medium
import nl.sogeti.android.gpstracker.ng.recording.RecordingViewModel.signalQualityLevel.none
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.rules.any
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit

class RecordingPresenterTest {

    lateinit var sut: RecordingPresenter
    lateinit var viewModel: RecordingViewModel
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
    lateinit var serviceManager: ServiceManager
    @Mock
    lateinit var trackUri: Uri
    @Mock
    lateinit var contentControllerFactory: ContentControllerFactory
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var gpsStatusControllerFactory: GpsStatusControllerFactory

    @Before
    fun setUp() {
        viewModel = RecordingViewModel(uri)
        sut = RecordingPresenter(viewModel)
        sut.serviceManager = serviceManager
        `when`(contentControllerFactory.createContentController(any(), any())).thenReturn(contentController)
        sut.contentControllerFactory = contentControllerFactory
        `when`(gpsStatusControllerFactory.createGpsStatusController(any(), any())).thenReturn(gpsStatusController)
        sut.gpsStatusControllerFactory = gpsStatusControllerFactory
    }

    @Test
    fun testStop() {
        // Arrange
        sut.start(context)
        sut.didConnectToService(trackUri, "mockTrack", ServiceConstants.STATE_LOGGING)
        // Act
        sut.willStop()
        // Assert
        verify(contentController).unregisterObserver();
        verify(gpsStatusController).stopUpdates();
    }

    @Test
    fun testConnectToLoggingService() {
        // Arrange
        sut.start(context)
        // Act
        sut.didConnectToService(uri, "mockTrack", ServiceConstants.STATE_LOGGING)
        // Assert
        Assert.assertThat(viewModel.isRecording.get(), `is`(true))
        Assert.assertThat(viewModel.name.get(), `is`("mockTrack"))
        Assert.assertThat(viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testConnectToPauseService() {
        // Arrange
        sut.start(context)
        // Act
        sut.didConnectToService(uri, "paused", ServiceConstants.STATE_PAUSED)
        // Assert
        Assert.assertThat(viewModel.isRecording.get(), `is`(true))
        Assert.assertThat(viewModel.name.get(), `is`("paused"))
        Assert.assertThat(viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testConnectToStoppedService() {
        // Arrange
        sut.start(context)
        // Act
        sut.didConnectToService(uri, "stopped", ServiceConstants.STATE_STOPPED)
        // Assert
        Assert.assertThat(viewModel.isRecording.get(), `is`(false))
        Assert.assertThat(viewModel.name.get(), `is`("stopped"))
        Assert.assertThat(viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testChangeToLoggingService() {
        // Arrange
        sut.start(context)
        // Act
        sut.didChangeLoggingState(uri, "mockTrack", ServiceConstants.STATE_STOPPED)
        // Assert
        Assert.assertThat(viewModel.isRecording.get(), `is`(false))
        Assert.assertThat(viewModel.name.get(), `is`("mockTrack"))
        Assert.assertThat(viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testOnChangeUriContent() {
        // Act
        sut.onChangeUriContent(uri, uri)
        // Assert
        assertThat(sut.executingReader, `is`(notNullValue()))
    }

    @Test
    fun testGpsStart() {
        // Act
        sut.onStart()
        // Assert
        assertThat(viewModel.isScanning.get(), `is`(true))
        assertThat(viewModel.hasFix.get(), `is`(false))
    }

    @Test
    fun testGpsStop() {
        // Act
        sut.onStop()
        // Assert
        assertThat(viewModel.isScanning.get(), `is`(false))
        assertThat(viewModel.hasFix.get(), `is`(false))
        assertThat(viewModel.maxSatellites.get(), `is`(0))
        assertThat(viewModel.currentSatellites.get(), `is`(0))
    }

    @Test
    fun onFirstFix(){
        // Act
        sut.onFirstFix()
        // Assert
        assertThat(viewModel.hasFix.get(), `is`(true))
        assertThat(viewModel.signalQuality.get(), `is`(4))
    }

    @Test
    fun onNoSignal() {
        // Act
        sut.onChange(0,0)
        // Assert
        assertThat(viewModel.maxSatellites.get(), `is`(0))
        assertThat(viewModel.currentSatellites.get(), `is`(0))
        assertThat(viewModel.signalQuality.get(), `is`(none))
    }

    @Test
    fun onLowSignal() {
        // Act
        sut.onChange(4,4)
        // Assert
        assertThat(viewModel.maxSatellites.get(), `is`(4))
        assertThat(viewModel.currentSatellites.get(), `is`(4))
        assertThat(viewModel.signalQuality.get(), `is`(low))
    }

    @Test
    fun onMediumSignal() {
        // Act
        sut.onChange(6,20)
        // Assert
        assertThat(viewModel.maxSatellites.get(), `is`(20))
        assertThat(viewModel.currentSatellites.get(), `is`(6))
        assertThat(viewModel.signalQuality.get(), `is`(medium))
    }

    @Test
    fun onHighSignal() {
        // Act
        sut.onChange(8,20)
        // Assert
        assertThat(viewModel.signalQuality.get(), `is`(high))
    }

    @Test
    fun onExcellentSignal() {
        // Act
        sut.onChange(10,20)
        // Assert
        assertThat(viewModel.signalQuality.get(), `is`(excellent))
    }
}
