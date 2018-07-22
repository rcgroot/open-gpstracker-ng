package nl.sogeti.android.gpstracker.ng.features.util

import android.content.ContentResolver
import android.net.Uri
import nl.sogeti.android.gpstracker.ng.base.BaseConfiguration
import nl.sogeti.android.gpstracker.ng.base.dagger.AppComponent
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.dagger.FeatureComponent
import nl.sogeti.android.gpstracker.service.dagger.ServiceComponent
import nl.sogeti.android.gpstracker.service.dagger.ServiceConfiguration
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class MockAppComponentTestRule : TestRule {

    lateinit var mockAppComponent: AppComponent

    lateinit var mockServiceComponent: ServiceComponent

    lateinit var mockFeatureComponent: FeatureComponent

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val mockUriBuilder = createMockBuilder()
                mockAppComponent = Mockito.mock(AppComponent::class.java)
                BaseConfiguration.appComponent = mockAppComponent
                `when`(mockAppComponent.uriBuilder()).thenReturn(mockUriBuilder)
                val mockResolver = mock(ContentResolver::class.java)
                `when`(mockAppComponent.contentResolver()).thenReturn(mockResolver)
                `when`(mockAppComponent.computationExecutor()).thenReturn(ImmediateExecutor())

                mockServiceComponent = Mockito.mock(ServiceComponent::class.java)
                ServiceConfiguration.serviceComponent = mockServiceComponent
                `when`(mockServiceComponent.providerAuthority()).thenReturn("mock-authority")

                mockFeatureComponent = Mockito.mock(FeatureComponent::class.java)
                FeatureConfiguration.featureComponent = mockFeatureComponent

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
