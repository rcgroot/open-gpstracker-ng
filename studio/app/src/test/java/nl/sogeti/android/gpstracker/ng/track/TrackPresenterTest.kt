package nl.sogeti.android.gpstracker.ng.track

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.BaseColumns._ID
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks.NAME
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.rules.any
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class TrackPresenterTest {

    lateinit var sut: TrackPresenter
    lateinit var viewModel: TrackViewModel
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var view: TrackViewModel.View
    @Mock
    lateinit var contentController: ContentController
    @Mock
    lateinit var contentControllerFactory: ContentControllerFactory
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var trackSelection: TrackSelection
    @Mock
    lateinit var trackUri: Uri
    @Mock
    lateinit var navigation: TrackNavigator

    @Before
    fun setUp() {
        viewModel = TrackViewModel()
        sut = TrackPresenter(viewModel, view)
        `when`(trackSelection.trackUri).thenReturn(trackUri)
        `when`(trackSelection.trackName).thenReturn("selected")
        sut.trackSelection = trackSelection
        Mockito.`when`(contentControllerFactory.createContentController(any(), any())).thenReturn(contentController)
        sut.contentControllerFactory = contentControllerFactory
    }

    @Test
    fun didStart() {
        // Act
        sut.start(context)
        // Assert
        verify(trackSelection).addListener(sut)
    }

    @Test
    fun willStop() {
        // Arrange
        sut.start(context)
        // Act
        sut.willStop()
        // Assert
        verify(trackSelection).removeListener(sut)
        verify(contentController).registerObserver(trackUri)
    }

    @Test
    fun testOptionSelected() {
        // Arrange
        sut.start(context, navigation)
        // Act
        sut.onListOptionSelected()
        // Assert
        verify(navigation).showTrackSelection()
    }

    @Test
    fun testAboutSelected() {
        // Arrange
        sut.start(context, navigation)
        // Act
        sut.onAboutOptionSelected()
        // Assert
        verify(navigation).showAboutDialog()
    }

    @Test
    fun testEditSelected() {
        // Arrange
        viewModel.trackUri.set(trackUri)
        sut.start(context, navigation)
        // Act
        sut.onEditOptionSelected()
        // Assert
        verify(navigation).showTrackEditDialog(trackUri)
    }

    @Test
    fun testContentChange() {
        // Arrange
        sut.start(context)
        val cursor = mock(Cursor::class.java)
        `when`(cursor.moveToFirst()).thenReturn(true)
        `when`(cursor.getColumnIndex(any())).thenReturn(1)
        `when`(cursor.getString(1)).thenReturn("mockname")
        val resolver = mock(ContentResolver::class.java)
        `when`(context.contentResolver).thenReturn(resolver)
        `when`(resolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
        // Act
        sut.onChangeUriContent(trackUri, trackUri)
        // Assert
        verify(view).showTrackName("mockname")
        assertThat(viewModel.name.get(), `is`("mockname"))
    }
}
