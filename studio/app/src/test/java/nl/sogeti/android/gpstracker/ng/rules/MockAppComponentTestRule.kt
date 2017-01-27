package nl.sogeti.android.gpstracker.ng.rules

import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.dagger.AppComponent
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mockito.Mockito

class MockAppComponentTestRule : TestRule {

    lateinit var mockAppComponent: AppComponent

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                mockAppComponent = Mockito.mock(AppComponent::class.java)
                GpsTrackerApplication.appComponent = mockAppComponent
                base.evaluate()
            }
        }
    }
}