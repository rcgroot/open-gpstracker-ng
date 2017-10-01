/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2017 Sogeti Nederland B.V. All Rights Reserved.
 **------------------------------------------------------------------------------
 ** Sogeti Nederland B.V.            |  No part of this file may be reproduced
 ** Distributed Software Engineering |  or transmitted in any form or by any
 ** Lange Dreef 17                   |  means, electronic or mechanical, for the
 ** 4131 NJ Vianen                   |  purpose, without the express written
 ** The Netherlands                  |  permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of OpenGPSTracker.
 *
 *   OpenGPSTracker is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OpenGPSTracker is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OpenGPSTracker.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.dagger

import android.net.Uri
import dagger.Component
import nl.sogeti.android.gpstracker.ng.control.ControlPresenter
import nl.sogeti.android.gpstracker.ng.graphs.GraphsPresenter
import nl.sogeti.android.gpstracker.ng.gpximport.GpxImportController
import nl.sogeti.android.gpstracker.ng.gpximport.ImportService
import nl.sogeti.android.gpstracker.ng.map.TrackMapPresenter
import nl.sogeti.android.gpstracker.ng.recording.RecordingNavigation
import nl.sogeti.android.gpstracker.ng.recording.RecordingPresenter
import nl.sogeti.android.gpstracker.ng.track.TrackNavigator
import nl.sogeti.android.gpstracker.ng.track.TrackPresenter
import nl.sogeti.android.gpstracker.ng.trackdelete.TrackDeletePresenter
import nl.sogeti.android.gpstracker.ng.trackedit.TrackEditPresenter
import nl.sogeti.android.gpstracker.ng.tracklist.ImportNotification
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListNavigation
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListPresenter
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListViewAdapter
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.utils.PermissionRequester
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(IntegrationModule::class, AppModule::class, SystemModule::class))
interface AppComponent {

    fun inject(injectable: TrackPresenter)

    fun inject(injectable: TrackMapPresenter)

    fun inject(injectable: TrackListPresenter)

    fun inject(injectable: TrackEditPresenter)

    fun inject(injectable: TrackDeletePresenter)

    fun inject(injectable: RecordingPresenter)

    fun inject(injectable: ControlPresenter)

    fun inject(injectable: GraphsPresenter)

    fun inject(injectable: SummaryCalculator)

    fun inject(injectable: TrackListViewAdapter)

    fun inject(injectable: SummaryManager)

    fun inject(injectable: PermissionRequester)

    fun inject(injectable: RecordingNavigation)

    fun inject(injectable: TrackNavigator)

    fun inject(injectable: TrackListNavigation)

    fun inject(injectable: GpxImportController)

    fun inject(injectable: ImportService)

    fun inject(inject: ImportNotification)

    @Named("providerAuthority")
    fun providerAuthority(): String

    @Named("shareProviderAuthority")

    fun providerShareAuthority(): String

    fun provideUriBuilder(): Uri.Builder
}
