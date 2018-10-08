/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.ng.base.common

import android.app.Application
import android.os.StrictMode
import androidx.annotation.CallSuper
import com.crashlytics.android.Crashlytics
import com.google.firebase.analytics.FirebaseAnalytics
import com.squareup.leakcanary.LeakCanary
import io.fabric.sdk.android.Fabric
import nl.sogeti.android.gpstracker.ng.base.BuildConfig
import timber.log.Timber


/**
 * Start app generic services
 */
open class BaseGpsTrackerApplication : Application() {

    var debug = BuildConfig.DEBUG

    @CallSuper
    override fun onCreate() {
        super.onCreate()
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // Running the app for the heap analyzer, not for the user
            return
        }
        setupCrashlitics()
        setupLeakCanary()
        setupLogging()
        setupAnalytics()
    }

    private fun setupCrashlitics() {
        if (BuildConfig.FLAVOR != "mock") {
            ofMainThread {
                Fabric.with(this, Crashlytics())
            }
        }
    }

    private fun setupAnalytics() {
        if (BuildConfig.FLAVOR == "mock") {
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
        }
    }

    private fun setupLeakCanary() {
        LeakCanary.install(this)
    }

    protected fun setupLogging() {
        if (debug) {
            Timber.plant(Timber.DebugTree())
            if (StrictMode.ThreadPolicy.Builder().build() != null) {
                StrictMode.setThreadPolicy(
                        StrictMode.ThreadPolicy.Builder()
                                .detectAll()
                                .penaltyLog()
                                .build())
            }
        }
    }

}
