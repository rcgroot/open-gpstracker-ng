package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceManager
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.any
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
        val sut = MyConnectedServicePresenter(mockServiceManager!!)

        // Execute
        sut.start(mockContext!!)

        // Verify
        verify(mockServiceManager)!!.startup(eq(mockContext), any())
        verify(mockContext)!!.registerReceiver(any(), any())
    }

    @Test
    fun willStop() {
        // Prepare
        val sut = MyConnectedServicePresenter(mockServiceManager!!)
        sut.start(mockContext!!)

        // Execute
        sut.willStop()

        // Verify
        verify(mockServiceManager)!!.shutdown(mockContext)
        verify(mockContext)!!.unregisterReceiver(any())
    }

    class MyConnectedServicePresenter(mockServiceManager: ServiceManager) : ConnectedServicePresenter(mockServiceManager) {

        var state = -1
        var uri: Uri? = null
        var manager: ServiceManager? = null

        override fun didChangeLoggingState(trackUri: Uri?, loggingState: Int) {
            this.uri = trackUri
            this.state = loggingState
        }

        override fun didConnectService(serviceManager: ServiceManager?) {
            this.manager = serviceManager
        }
    }
}