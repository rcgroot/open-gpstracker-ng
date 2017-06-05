package nl.sogeti.android.gpstracker.ng.trackedit

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.view.View
import android.widget.AdapterView
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import nl.sogeti.android.gpstracker.ng.rules.any
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class TrackEditPresenterTest {

    lateinit var sut: TrackEditPresenter
    lateinit var viewModel: TrackEditModel
    @get:Rule
    val mockitoRule = MockitoJUnit.rule()
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var view: TrackEditModel.View
    @Mock
    lateinit var trackUri: Uri
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var contentResolver: ContentResolver
    @Mock
    lateinit var summaryManager: SummaryManager
    @Mock
    lateinit var trackTypeDescriptions: TrackTypeDescriptions
    @Mock
    lateinit var cursor: Cursor

    @Before
    fun setUp() {
        `when`(trackUri.lastPathSegment).thenReturn("6")
        viewModel = TrackEditModel(trackUri)
        viewModel.selectedPosition.set(1)
        sut = TrackEditPresenter(viewModel, view)
        sut.summaryManager = summaryManager
        sut.trackTypeDescriptions = trackTypeDescriptions
        `when`(context.contentResolver).thenReturn(contentResolver)
        `when`(contentResolver.query(any(), any(), any(), any(), any())).thenReturn(cursor)
    }

    @Test
    fun testStart() {
        // Arrange
        `when`(trackTypeDescriptions.loadTrackType(context, trackUri)).thenReturn(TrackTypeDescriptions.allTrackTypes.get(4))
        `when`(cursor.moveToFirst()).thenReturn(true)
        `when`(cursor.getColumnIndex(any())).thenReturn(0)
        `when`(cursor.getString(0)).thenReturn("mockname")
        // Act
        sut.start(context)
        // Verify
        assertThat(viewModel.selectedPosition.get(), `is`(4))
        assertThat(viewModel.name.get(), `is`("mockname"))
    }

    @Test
    fun testOk() {
        // Arrange
        sut.start(context)
        sut.model.selectedPosition.set(1)
        // Act
        sut.ok()
        // Assert
        verify(view).dismiss()
        verify(summaryManager).removeFromCache(trackUri)
    }

    @Test
    fun testCancel() {
        // Act
        sut.cancel()
        // Assert
        verify(view).dismiss()
    }

    @Test
    fun testSelect() {
        // Arrange
        val adapterView = mock(AdapterView::class.java)
        val view = mock(View::class.java)
        // Act
        sut.onItemSelectedListener.onItemSelected(adapterView, view, 4, 5);
        // Assert
        assertThat(viewModel.selectedPosition.get(), `is`(4))
    }

    @Test
    fun testSelectNothing() {
        // Arrange
        val adapterView = mock(AdapterView::class.java)
        // Act
        sut.onItemSelectedListener.onNothingSelected(adapterView)
        // Assert
        assertThat(viewModel.selectedPosition.get(), `is`(-1))
    }
}
