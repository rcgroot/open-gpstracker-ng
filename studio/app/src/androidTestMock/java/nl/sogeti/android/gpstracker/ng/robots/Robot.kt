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
import android.graphics.Bitmap
import android.os.Environment
import android.os.SystemClock
import android.support.test.InstrumentationRegistry
import android.support.test.InstrumentationRegistry.getInstrumentation
import android.support.test.espresso.Espresso
import android.support.test.uiautomator.UiDevice
import android.support.v4.content.ContextCompat
import nl.sogeti.android.gpstracker.ng.util.MockServiceManager
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


open class Robot<T : Robot<T>>(private val screenName: String) {

    companion object {
        var shotsFired = 0
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

    fun waitForIdle() {
        getInstrumentation().waitForIdleSync()
    }

    fun back(): T {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.pressBack()

        return this as T
    }

    private fun shoot(): File {
        val file = nextShotFile()
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.takeScreenshot(file)

        return file
    }

    fun sleep(seconds: Int): T {
        SystemClock.sleep(seconds * 1000L)

        return this as T
    }

    private fun write(bitmap: Bitmap, file: File) {
        try {
            var out: FileOutputStream? = null
            var bout: BufferedOutputStream? = null
            try {
                out = FileOutputStream(file)
                bout = BufferedOutputStream(out, 8194)
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, bout);
                bout.flush()
                out.flush()
            } finally {
                bout?.close()
                out?.close()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to write bitmap")
        }
    }

    private fun nextShotFile(): File {
        val context = getInstrumentation().context
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            throw IllegalStateException("Storage permission is not granted. Check App info")
        }

        var file : File? = null
        val fileName = "${screenName}_$shotsFired"
        val root = Environment.getExternalStorageDirectory()
        if (root != null && root.exists()) {
            val path = File(root, "screenshots")
            var exists = true
            if (!path.exists()) {
                exists = path.mkdir()
            }
            if (exists) {
                shotsFired++
                file = File(path, "$fileName.png")
                Timber.d("Created new file $file")
                file.createNewFile()
            }
            else {
                Timber.e("Failed to create directory $path")
            }
        }
        else {
            Timber.e("Missing directory $root")
        }
        if (file == null) {
            throw IllegalStateException("Should have created a file")
        }

        return file
    }
}