package nl.sogeti.android.gpstracker.ng.features.dagger

import dagger.Component
import nl.sogeti.android.gpstracker.ng.base.dagger.AppComponent
import nl.sogeti.android.gpstracker.ng.features.about.AboutModel
import nl.sogeti.android.gpstracker.ng.features.activityrecognition.ActivityRecognizerLoggingBroadcastReceiver
import nl.sogeti.android.gpstracker.ng.features.control.ControlFragment
import nl.sogeti.android.gpstracker.ng.features.control.ControlPresenter
import nl.sogeti.android.gpstracker.ng.features.gpximport.GpxImportController
import nl.sogeti.android.gpstracker.ng.features.gpximport.ImportService
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphLabelsBindings
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphsFragment
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphsPresenter
import nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders.DistanceDataProvider
import nl.sogeti.android.gpstracker.ng.features.graphs.dataproviders.TimeDataProvider
import nl.sogeti.android.gpstracker.ng.features.map.TrackMapFragment
import nl.sogeti.android.gpstracker.ng.features.map.TrackMapPresenter
import nl.sogeti.android.gpstracker.ng.features.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingBindingAdapters
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingFragment
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingNavigation
import nl.sogeti.android.gpstracker.ng.features.recording.RecordingPresenter
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.features.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.features.track.TrackActivity
import nl.sogeti.android.gpstracker.ng.features.track.TrackNavigator
import nl.sogeti.android.gpstracker.ng.features.track.TrackPresenter
import nl.sogeti.android.gpstracker.ng.features.trackdelete.TrackDeletePresenter
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackEditPresenter
import nl.sogeti.android.gpstracker.ng.features.tracklist.*
import nl.sogeti.android.gpstracker.ng.features.wear.PhoneMessageListenerService
import nl.sogeti.android.gpstracker.ng.features.wear.StatisticsCollector
import nl.sogeti.android.gpstracker.ng.features.wear.WearLoggingService
import nl.sogeti.android.gpstracker.ng.features.wear.WearLoggingStateBroadcastReceiver
import nl.sogeti.android.gpstracker.service.dagger.ServiceComponent
import javax.inject.Named

@FeatureScope
@Component(modules = [FeatureModule::class, VersionInfoModule::class],
        dependencies = [AppComponent::class, ServiceComponent::class])
interface FeatureComponent {

    @Named("shareProviderAuthority")
    fun providerShareAuthority(): String
    fun trackSelection(): TrackSelection

    fun inject(injectable: TrackDeletePresenter)
    fun inject(injectable: TrackEditPresenter)
    fun inject(injectable: GraphsPresenter)
    fun inject(injectable: TrackPresenter)
    fun inject(injectable: TrackMapPresenter)
    fun inject(injectable: TrackListPresenter)
    fun inject(injectable: RecordingPresenter)

    fun inject(trackNavigator: TrackNavigator)
    fun inject(gpxImportController: GpxImportController)
    fun inject(importService: ImportService)
    fun inject(aboutModel: AboutModel)
    fun inject(controlPresenter: ControlPresenter)
    fun inject(recordingNavigation: RecordingNavigation)
    fun inject(importNotification: ImportNotification)
    fun inject(trackListNavigation: TrackListNavigation)
    fun inject(trackListViewAdapter: TrackListViewAdapter)
    fun inject(summaryCalculator: SummaryCalculator)
    fun inject(summaryManager: SummaryManager)
    fun inject(wearLoggingService: WearLoggingService)
    fun inject(phoneMessageListenerService: PhoneMessageListenerService)
    fun inject(statisticsCollector: StatisticsCollector)
    fun inject(graphLabelsBindings: GraphLabelsBindings)
    fun inject(timeProvider: TimeDataProvider)
    fun inject(distanceDataProvider: DistanceDataProvider)
    fun inject(controlFragment: ControlFragment)
    fun inject(trackListActivity: TrackListActivity)
    fun inject(trackActivity: TrackActivity)
    fun inject(receiver: WearLoggingStateBroadcastReceiver)
    fun inject(recordingBindingAdapters: RecordingBindingAdapters)
    fun inject(receiver: ActivityRecognizerLoggingBroadcastReceiver)
    fun inject(recordingFragment: RecordingFragment)
    fun inject(graphsFragment: GraphsFragment)
    fun inject(trackMapFragment: TrackMapFragment)
    fun inject(trackListFragment: TrackListFragment)
}
