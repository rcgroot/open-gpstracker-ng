package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.databinding.Observable
import android.databinding.ObservableField
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceManager
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.TrackObservingPresenter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.eq
import org.mockito.Matchers.notNull
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class TrackObservingPresenterTest {

    @Mock
    var mockContext: Context? = null
    @Mock
    var mockResolver: ContentResolver? = null
    @Mock
    var mockField: ObservableField<Uri?>? = null
    @Mock
    var mockUri: Uri? = null
    private var sut: TrackObservingPresenter? = null

    @Before
    fun setup() {
        sut = MyTrackObservingPresenter(mockField!!)
        `when`(mockContext!!.contentResolver).thenReturn(mockResolver)
        `when`(mockField!!.get()).thenReturn(mockUri)
    }

    @Test
    fun testDidStartWillRegister() {
        // Prepare

        // Execute
        sut?.start(mockContext!!)

        // Verify
        verify(mockResolver)!!.registerContentObserver(eq(mockUri), eq(true), notNull(ContentObserver::class.java))
        verify(mockField)!!.addOnPropertyChangedCallback(notNull(Observable.OnPropertyChangedCallback::class.java))
    }

    @Test
    fun testWillStopWillUnregister() {
        // Prepare
        sut?.start(mockContext!!)
        Mockito.reset(mockResolver)
        Mockito.reset(mockField)

        // Execute
        sut?.stop()

        // Verify
        verify(mockResolver)!!.unregisterContentObserver(notNull(ContentObserver::class.java))
        verify(mockField)!!.removeOnPropertyChangedCallback(notNull(Observable.OnPropertyChangedCallback::class.java))
    }

    class MyTrackObservingPresenter(val field: ObservableField<Uri?>) : TrackObservingPresenter() {
        override fun getTrackUriField(): ObservableField<Uri?> {
            return field
        }

        override fun didChangeUriContent(uri: Uri, includingUri: Boolean) {
        }

        override fun didConnectService(serviceManager: ServiceManager?) {
        }

        override fun didChangeLoggingState(trackUri: Uri?, loggingState: Int) {
        }

    }

}