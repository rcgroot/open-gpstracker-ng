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

import android.app.Application
import dagger.Module
import dagger.Provides
import nl.renedegroot.android.concurrent.ExecutorFactory
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.gpxexport.GpxShareProvider
import nl.sogeti.android.gpstracker.ng.gpxexport.ShareIntentFactory
import nl.sogeti.android.gpstracker.ng.gpximport.GpxImportController
import nl.sogeti.android.gpstracker.ng.gpximport.GpxParser
import nl.sogeti.android.gpstracker.ng.map.TrackReaderFactory
import nl.sogeti.android.gpstracker.ng.map.rendering.TrackTileProviderFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.tracklist.ImportNotification
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.wear.StatisticsCollector
import nl.sogeti.android.gpstracker.v2.sharedwear.messaging.MessageSenderFactory
import nl.sogeti.android.gpstracker.v2.sharedwear.util.StatisticsFormatting
import nl.sogeti.android.gpstracker.v2.sharedwear.util.TimeSpanCalculator
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Named
import javax.inject.Singleton


@Module
class AppModule(val application: Application) {


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

    @Provides
    @Named("shareProviderAuthority")
    fun shareProviderAuthority(): String {
        return GpxShareProvider.AUTHORITY
    }

    @Provides
    @Named("dayFormatter")
    fun dayFormatter(locale: Locale) = SimpleDateFormat("EEEE", locale)

    @Provides
    fun gpxParser() = GpxParser(application)

    @Provides
    fun gpxImportController() = GpxImportController(application)

    @Provides
    @Singleton
    fun importNotification() = ImportNotification(application)

    @Provides
    fun messageSenderFactory() = MessageSenderFactory()

    @Provides
    fun executorFactory() = ExecutorFactory()

    @Provides
    fun statisticsCollector() = StatisticsCollector()

    @Provides
    fun statisticsFormatting(locale: Locale, timeSpanUtil: TimeSpanCalculator) = StatisticsFormatting(locale, timeSpanUtil)
}
