package nl.sogeti.android.gpstracker.ng.features.dagger

import dagger.Module
import dagger.Provides
import javax.inject.Named

@FeatureScope
@Module
class VersionInfoModule(private val version: String, private val commit: String, private val buildNumber: String) {

    @Provides
    @Named("version")
    fun provideVersion() = version

    @Provides
    @Named("commit")
    fun provideCommit() = commit

    @Provides
    @Named("buildNumber")
    fun provideBuildNumber() = buildNumber
}
