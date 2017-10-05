package nl.sogeti.android.gpstracker.ng.tracklist

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.rules.any
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.utils.tracksUri
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit
import java.util.concurrent.Executor

class TrackListPresenterTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var view: TrackListViewModel.View
    lateinit var viewModel: TrackListViewModel
    @Mock
    lateinit var trackSelection: TrackSelection
    @Mock
    lateinit var contentControllerFactory: ContentControllerFactory
    @Mock
    lateinit var summaryManager: SummaryManager
    @Mock
    lateinit var contentController: ContentController
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var executor: Executor
    @Mock
    lateinit var navigation: TrackListNavigation
    @Mock
    lateinit var notificationFactory: ImportNotificationFactory
    @Mock
    lateinit var notification: ImportNotification
    lateinit var sut: TrackListPresenter

    @Before
    fun setUp() {
        viewModel = TrackListViewModel()
        sut = TrackListPresenter(viewModel, view)
        sut.trackSelection = trackSelection
        sut.contentControllerFactory = contentControllerFactory
        sut.summaryManager = summaryManager
        sut.executor = executor
        sut.notificationFactory = notificationFactory
        `when`(contentControllerFactory.createContentController(any(), any()))
                .thenReturn(contentController)
        `when`(notificationFactory.createImportNotification(context))
                .thenReturn(notification)
    }

    @Test
    fun testStart() {
        // Act
        sut.start(context)
        // Assert
        verify(summaryManager).start()
        verify(contentController).registerObserver(tracksUri())
    }

    @Test
    fun testStop() {
        // Arrange
        sut.start(context)
        // Act
        sut.willStop()
        // Assert
        verify(contentController).unregisterObserver()
    }

    @Test
    fun testTrackSelection() {
        // Arrange
        val selectedUri = mock(Uri::class.java)
        sut.start(context, navigation)
        // Act
        sut.didSelectTrack(selectedUri, "testname")
        // Assert
        verify(trackSelection).selectTrack(selectedUri, "testname")
        verify(navigation).finishTrackSelection()
    }
}
