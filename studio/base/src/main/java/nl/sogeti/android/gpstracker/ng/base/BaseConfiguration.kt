package nl.sogeti.android.gpstracker.ng.base

import android.app.Application
import nl.sogeti.android.gpstracker.ng.base.dagger.AppComponent
import nl.sogeti.android.gpstracker.ng.base.dagger.AppModule
import nl.sogeti.android.gpstracker.ng.base.dagger.DaggerAppComponent
import nl.sogeti.android.gpstracker.ng.base.dagger.SystemModule

object BaseConfiguration {

    lateinit var appComponent: AppComponent

    fun initAppComponent(application: Application) {
        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(application))
                .systemModule(SystemModule())
                .build()
    }
}
