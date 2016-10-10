package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceManager
import nl.sogeti.android.gpstracker.integration.ServiceManagerInterface
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.runners.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ConnectedServicePresenterTest {

    @Mock
    var mockServiceManager: ServiceManager? = null
    @Mock
    var mockContext: Context? = null

    @Test
    fun didStart() {
        // Prepare
        val sut = MyConnectedServicePresenter()

        // Execute
        sut.start(mockContext!!)

        // Verify
        verify(mockServiceManager)!!.startup(eq(mockContext!!), any())
        verify(mockContext)!!.registerReceiver(any(), any())
    }

    @Test
    fun willStop() {
        // Prepare
        val sut = MyConnectedServicePresenter()
        sut.start(mockContext!!)

        // Execute
        sut.willStop()

        // Verify
        verify(mockServiceManager)!!.shutdown(mockContext!!)
        verify(mockContext)!!.unregisterReceiver(ArgumentMatchers.any())
    }

    class MyConnectedServicePresenter() : ConnectedServicePresenter() {

        var state = -1
        var uri: Uri? = null

        override fun didChangeLoggingState(trackUri: Uri, loggingState: Int) {
            this.uri = trackUri
            this.state = loggingState
        }

        override fun didConnectService(serviceManager: ServiceManagerInterface?) {
        }
    }
}