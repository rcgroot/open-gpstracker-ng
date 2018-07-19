/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import dagger.Module
import dagger.Provides
import nl.sogeti.android.gpstracker.ng.base.dagger.Computation
import nl.sogeti.android.gpstracker.ng.base.dagger.DiskIO
import nl.sogeti.android.gpstracker.ng.base.dagger.NetworkIO
import nl.sogeti.android.gpstracker.ng.base.location.LocationFactory
import nl.sogeti.android.gpstracker.ng.common.controllers.gpsstatus.GpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.mock.MockGpsStatusControllerFactory
import nl.sogeti.android.gpstracker.ng.mock.MockLocationFactory
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class MockSystemModule {

    @Provides
    fun locale(): Locale {
        return Locale.getDefault()
    }

    @Provides
    fun gpsStatusControllerFactory(application: Context): GpsStatusControllerFactory {
        return MockGpsStatusControllerFactory(application)
    }

    @Provides
    fun uriBuilder() = Uri.Builder()

    @Provides
    @Computation
    fun executor() = AsyncTask.THREAD_POOL_EXECUTOR

    @Provides
    @Singleton
    @DiskIO
    fun diskExecutor(): Executor = ThreadPoolExecutor(1, 2, 10L, TimeUnit.SECONDS, LinkedBlockingQueue())

    @Provides
    @Singleton
    @NetworkIO
    fun networkExecutor(): Executor = ThreadPoolExecutor(1, 16, 30L, TimeUnit.SECONDS, LinkedBlockingQueue())

    @Provides
    fun packageManager(application: Context): PackageManager = application.packageManager

    @Provides
    fun locationFactory(): LocationFactory = MockLocationFactory()

    @Provides
    fun contentResolver(application: Context): ContentResolver = application.contentResolver
}
