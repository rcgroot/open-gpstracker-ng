package nl.sogeti.android.gpstracker.ng.common.controllers

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentController
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnit

class ContentControllerTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @Mock
    lateinit var registerUri: Uri
    @Mock
    lateinit var context: Context
    @Mock
    lateinit var contentResolver: ContentResolver
    @Mock
    lateinit var listener: ContentController.Listener

    @Captor
    lateinit var uriCaptor: ArgumentCaptor<Uri>
    @Captor
    lateinit var booleanCaptor: ArgumentCaptor<Boolean>
    @Captor
    lateinit var contentObserverCaptor: ArgumentCaptor<ContentObserver>

    lateinit var sut: ContentController

    @Before
    fun setUp() {
        sut = ContentController(context, listener)
        `when`(context.contentResolver).thenReturn(contentResolver)
    }

    @Test
    fun testRegisterObserver() {
        // Act
        sut.registerObserver(registerUri)
        // Assert
        verify(contentResolver).registerContentObserver(uriCaptor.capture(), booleanCaptor.capture(), contentObserverCaptor.capture())
        assertThat(uriCaptor.value, `is`(notNullValue()))
        assertThat(booleanCaptor.value, `is`(notNullValue()))
        assertThat(contentObserverCaptor.value, `is`(notNullValue()))
        assertThat(uriCaptor.value, `is`(registerUri))
        assertThat(booleanCaptor.value, `is`(true))
    }

    @Test
    fun testCallback() {
        // Arrange
        val changedUri = mock(Uri::class.java)
        sut.registerObserver(registerUri)
        verify(contentResolver).registerContentObserver(uriCaptor.capture(), booleanCaptor.capture(), contentObserverCaptor.capture())
        // Act
        contentObserverCaptor.value.onChange(false, changedUri)
        // Assert
        verify(listener).onChangeUriContent(registerUri, changedUri)
    }

    @Test
    fun testUnregisterObserver() {
        // Arrange
        val changedUri = mock(Uri::class.java)
        sut.registerObserver(registerUri)
        sut.unregisterObserver()
        verify(contentResolver).registerContentObserver(uriCaptor.capture(), booleanCaptor.capture(), contentObserverCaptor.capture())
        val contentObserver = contentObserverCaptor.value
        // Act
        contentObserver.onChange(false, changedUri)
        // Assert
        verify(context.contentResolver).unregisterContentObserver(contentObserver)
        verify(listener, never()).onChangeUriContent(registerUri, changedUri)
    }

}