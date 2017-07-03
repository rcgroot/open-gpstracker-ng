package nl.sogeti.android.gpstracker.ng.common.abstractpresenters

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ServiceManager
import nl.sogeti.android.gpstracker.ng.rules.MockAppComponentTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.eq
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit

class ConnectedServicePresenterTest {

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()
    @get:Rule
    var appComponentRule = MockAppComponentTestRule()
    @Mock
    lateinit var mockServiceManager: ServiceManager
    @Mock
    lateinit var mockContext: Context

    lateinit var sut: MyConnectedServicePresenter

    @Before
    fun setUp() {

        sut = MyConnectedServicePresenter()
        sut.serviceManager = mockServiceManager
    }

    @Test
    fun didStart() {
        // Execute
        sut.start(mockContext)

        // Verify
        verify(mockServiceManager).startup(eq(mockContext), any())
        verify(mockContext).registerReceiver(any(), any())
    }

    @Test
    fun willStop() {
        // Prepare
        sut.start(mockContext)

        // Execute
        sut.willStop()

        // Verify
        verify(mockServiceManager).shutdown(mockContext)
        verify(mockContext).unregisterReceiver(ArgumentMatchers.any())
    }

    class MyConnectedServicePresenter : ConnectedServicePresenter<Navigation>() {

        var state = -1
        var uri: Uri? = null

        override fun didConnectToService(trackUri: Uri?, name: String?, loggingState: Int) {
            this.uri = trackUri
            this.state = loggingState
        }
        override fun didChangeLoggingState(trackUri: Uri?, name: String?, loggingState: Int) {
            this.uri = trackUri
            this.state = loggingState
        }
    }
}