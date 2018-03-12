package nl.sogeti.android.gpstracker.ng.features.track

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import nl.renedegroot.android.test.utils.any
import nl.sogeti.android.gpstracker.ng.base.common.controllers.content.ContentController
import nl.sogeti.android.gpstracker.ng.base.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.features.util.MockAppComponentTestRule
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
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var contentController: ContentController
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var trackSelection: TrackSelection
    @Mock
    lateinit var trackUri: Uri
    @Mock
    lateinit var navigation: TrackNavigator
    @Mock
    lateinit var trackTypeDescriptions: TrackTypeDescriptions

    @Before
    fun setUp() {
        sut = TrackPresenter(trackTypeDescriptions, trackSelection, contentController)
        sut.navigation = navigation
        `when`(trackSelection.trackUri).thenReturn(trackUri)
        `when`(trackSelection.trackName).thenReturn("selected")
    }

    @Test
    fun didStart() {
        // Act
        sut.start()
        // Assert
        verify(trackSelection).addListener(sut)
    }

    @Test
    fun willStop() {
        // Act
        sut.onCleared()
        // Assert
        verify(trackSelection).removeListener(sut)
        verify(contentController).unregisterObserver()
    }

    @Test
    fun testOptionSelected() {
        // Arrange
        sut.start()
        // Act
        sut.onListOptionSelected()
        // Assert
        verify(navigation).showTrackSelection()
    }

    @Test
    fun testAboutSelected() {
        // Arrange
        sut.start()
        // Act
        sut.onAboutOptionSelected()
        // Assert
        verify(navigation).showAboutDialog()
    }

    @Test
    fun testEditSelected() {
        // Arrange
        sut.viewModel.trackUri.set(trackUri)
        sut.start()
        // Act
        sut.onEditOptionSelected()
        // Assert
        verify(navigation).showTrackEditDialog(trackUri)
    }
}
