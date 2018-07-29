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
package nl.sogeti.android.gpstracker.ng.robots

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.SystemClock
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.uiautomator.UiDevice
import android.support.v4.content.ContextCompat
import nl.sogeti.android.gpstracker.service.mock.MockServiceManager
import timber.log.Timber
import java.io.File


open class Robot<T : Robot<T>>(private val screenName: String) {

    fun back(): T {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressBack()

        return this as T
    }

    fun waitForIdle() {
        getInstrumentation().waitForIdleSync()
    }

    fun sleep(seconds: Int): T {
        SystemClock.sleep(seconds * 1000L)

        return this as T
    }

    fun takeScreenShot(): T {
        waitForIdle()
        MockServiceManager.pauseWaypointGenerations = true
        sleep(1)
        val file = shoot()
        MockServiceManager.pauseWaypointGenerations = false
        Timber.w("Created file ${file.absoluteFile}")

        return this as T
    }

    private fun shoot(): File {
        val file = nextShotFile(screenName)
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.takeScreenshot(file)

        return file
    }

    companion object {
        fun resetScreenShots() {
            shotsFired = 0
            screenshotsDirectory()?.deleteRecursively()
        }

        private var shotsFired = 0

        private fun nextShotFile(screenName: String): File {
            val context = getInstrumentation().context
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw IllegalStateException("Storage permission is not granted. Check App info")
            }

            val directory = create(screenshotsDirectory())

            shotsFired++
            val file = File(directory, String.format("%03d %s.png", shotsFired, screenName))
            Timber.d("Created new file $file")
            file.createNewFile()


            return file
        }

        private fun create(path: File?): File {
            if (path == null) {
                throw IllegalStateException("Missing path")
            }
            var exists = path.exists()
            if (!exists) {
                exists = path.mkdir()
                if (!exists) {
                    throw IllegalStateException("Failed to create directory $path")
                }
            }

            return path
        }

        private fun screenshotsDirectory(): File? {
            val file = getContext().filesDir.child("test-screenshots")
            Timber.d("Storing screenshots in $file")

            return file
        }

        private fun getContext(): Context {
            return InstrumentationRegistry.getTargetContext()
        }

        private fun File?.child(dir: String): File? {
            return File(this, dir)
        }
    }

}

