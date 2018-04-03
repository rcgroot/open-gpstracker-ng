package nl.sogeti.android.gpstracker.ng.base.dagger

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DiskIO

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class NetworkIO

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Computation