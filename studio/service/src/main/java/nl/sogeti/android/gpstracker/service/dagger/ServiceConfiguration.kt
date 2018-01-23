package nl.sogeti.android.gpstracker.service.dagger

object ServiceConfiguration {

    lateinit var serviceComponent: ServiceComponent

    fun initServiceComponent() {
        serviceComponent = DaggerServiceComponent.builder()
                .serviceModule(ServiceModule())
                .build()
    }
}
