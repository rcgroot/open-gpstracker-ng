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

import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.sharing.GpxShareProvider
import nl.sogeti.android.gpstracker.ng.sharing.ShareIntentFactory
import nl.sogeti.android.gpstracker.ng.map.LocationFactory
import nl.sogeti.android.gpstracker.ng.map.TrackReaderFactory
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackTileProviderFactory
import nl.sogeti.android.gpstracker.ng.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.tracklist.summary.TimeSpanCalculator
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    fun timeSpanCalculator() = TimeSpanCalculator()

    @Singleton
    @Provides
    fun trackSelection() = TrackSelection()

    @Provides
    fun contentControllerFactory() = ContentControllerFactory()

    @Singleton
    @Provides
    fun summaryManager() = SummaryManager()

    @Provides
    fun summaryCalculator() = SummaryCalculator()

    @Provides
    fun trackReaderFactory() = TrackReaderFactory()

    @Provides
    fun trackTileProviderFactory() = TrackTileProviderFactory()

    @Provides
    fun trackTypeDescriptions() = TrackTypeDescriptions()

    @Provides
    fun shareIntentFactory() = ShareIntentFactory()

    @Provides @Named("shareProviderAuthority")
    fun shareProviderAuthority(): String {
        return GpxShareProvider.AUTHORITY
    }

    @Provides @Named("dayFormatter")
    fun dayFormatter() = SimpleDateFormat("EEEE", Locale.getDefault())
}
