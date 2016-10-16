package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.ContentResolver
import android.content.Context
import android.databinding.ObservableField
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.ArgumentMatchers.notNull
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
        verify(mockResolver)!!.registerContentObserver(eq(mockUri), eq(true), notNull())
        verify(mockField)!!.addOnPropertyChangedCallback(notNull())
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
        verify(mockResolver)!!.unregisterContentObserver(notNull())
        verify(mockField)!!.removeOnPropertyChangedCallback(notNull())
    }

    class MyTrackObservingPresenter(val field: ObservableField<Uri?>) : TrackObservingPresenter() {

        override fun getTrackUriField(): ObservableField<Uri?> {
            return field
        }

        override fun onChangeUriField(uri: Uri) {
        }

        override fun onChangeUriContent(uri: Uri){
        }

        override fun didConnectService(serviceManager: ServiceManagerInterface?) {
        }

        override fun didChangeLoggingState(trackUri: Uri, loggingState: Int) {
        }

    }

}