package nl.sogeti.android.gpstracker.ng.recording

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceConstants
import nl.sogeti.android.gpstracker.integration.ServiceManager
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerProvider
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusController
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerProvider
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.rules.any
import org.hamcrest.CoreMatchers.`is`
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
    lateinit var contentControllerProvider: ContentControllerProvider
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var gpsStatusControllerProvider: GpsStatusControllerProvider

    @Before
    fun setUp() {
        viewModel = RecordingViewModel(uri)
        sut = RecordingPresenter(viewModel)
        sut.serviceManager = serviceManager
        sut.context = context
        `when`(contentControllerProvider.createContentControllerProvider(any(), any())).thenReturn(contentController)
        sut.contentControllerProvider = contentControllerProvider
        `when`(gpsStatusControllerProvider.createGpsStatusListenerProvider(any(), any())).thenReturn(gpsStatusController)
        sut.gpsStatusControllerProvider = gpsStatusControllerProvider
    }

    @Test
    fun testStop() {
        // Arrange
        sut.didConnectToService(trackUri, "mockTrack", ServiceConstants.STATE_LOGGING)
        // Act
        sut.willStop()
        // Assert
        verify(contentController).unregisterObserver();
        verify(gpsStatusController).stopUpdates();
    }

    @Test
    fun testConnectToLoggingService() {
        // Act
        sut.didConnectToService(uri, "mockTrack", ServiceConstants.STATE_LOGGING)
        // Assert
        Assert.assertThat(viewModel.isRecording.get(), `is`(true))
        Assert.assertThat(viewModel.name.get(), `is`("mockTrack"))
        Assert.assertThat(viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testConnectToPauseService() {
        // Act
        sut.didConnectToService(uri, "paused", ServiceConstants.STATE_PAUSED)
        // Assert
        Assert.assertThat(viewModel.isRecording.get(), `is`(true))
        Assert.assertThat(viewModel.name.get(), `is`("paused"))
        Assert.assertThat(viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testConnectToStoppedService() {
        // Act
        sut.didConnectToService(uri, "stopped", ServiceConstants.STATE_STOPPED)
        // Assert
        Assert.assertThat(viewModel.isRecording.get(), `is`(false))
        Assert.assertThat(viewModel.name.get(), `is`("stopped"))
        Assert.assertThat(viewModel.trackUri.get(), `is`(uri))
    }

    @Test
    fun testChangeToLoggingService() {
        // Act
        sut.didChangeLoggingState(uri, "mockTrack", ServiceConstants.STATE_STOPPED)
        // Assert
        Assert.assertThat(viewModel.isRecording.get(), `is`(false))
        Assert.assertThat(viewModel.name.get(), `is`("mockTrack"))
        Assert.assertThat(viewModel.trackUri.get(), `is`(uri))
    }

}