package nl.sogeti.android.gpstracker.ng.rules

import android.net.Uri
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.dagger.AppComponent
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any

class MockAppComponentTestRule : TestRule {

    lateinit var mockAppComponent: AppComponent

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val mockUriBuilder = createMockBuilder()
                mockAppComponent = Mockito.mock(AppComponent::class.java)
                GpsTrackerApplication.appComponent = mockAppComponent
                `when`(mockAppComponent.providerAuthority()).thenReturn("mock-authority")
                `when`(mockAppComponent.provideUriBuilder()).thenReturn(mockUriBuilder)
                base.evaluate()
            }
        }
    }

    private fun createMockBuilder(): Uri.Builder {
        val builder = Mockito.mock(Uri.Builder::class.java)
        val uri = Mockito.mock(Uri::class.java)
        `when`(builder.scheme(ArgumentMatchers.any())).thenReturn(builder)
        `when`(builder.authority(ArgumentMatchers.any())).thenReturn(builder)
        `when`(builder.appendPath(ArgumentMatchers.any())).thenReturn(builder)
        `when`(builder.appendEncodedPath(ArgumentMatchers.any())).thenReturn(builder)
        `when`(builder.build()).thenReturn(uri)

        return builder
    }
}
