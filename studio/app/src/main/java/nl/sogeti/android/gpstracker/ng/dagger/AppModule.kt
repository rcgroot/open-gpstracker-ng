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
import nl.sogeti.android.gpstracker.ng.utils.PermissionRequester
import nl.sogeti.android.gpstracker.ng.common.controllers.content.ContentControllerFactory
import nl.sogeti.android.gpstracker.ng.model.TrackSelection
import nl.sogeti.android.gpstracker.ng.track.map.LocationFactory
import nl.sogeti.android.gpstracker.ng.track.map.TrackReaderFactory
import nl.sogeti.android.gpstracker.ng.track.map.rendering.TrackTileProviderFactory
import nl.sogeti.android.gpstracker.ng.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryCalculator
import nl.sogeti.android.gpstracker.ng.tracklist.summary.SummaryManager
import nl.sogeti.android.gpstracker.ng.tracklist.summary.TimeSpanCalculator
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
    fun locationFactory() = LocationFactory()
}