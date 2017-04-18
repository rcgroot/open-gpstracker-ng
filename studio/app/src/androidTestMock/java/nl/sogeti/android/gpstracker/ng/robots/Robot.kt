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

import android.graphics.Bitmap
import android.os.Environment
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.uiautomator.UiDevice
import timber.log.Timber
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


open class Robot<T : Robot<T>>(private val screenName: String) {

    companion object {
        var shotsFired = 0
    }

    fun takeScreenShot(): T {
        onView(withId(android.R.id.content)).check(matches(isDisplayed()))
        val file = shoot()
        Timber.w("Created file ${file.absoluteFile}")


        return this as T
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
        shotsFired++
        val fileName = "${screenName}_$shotsFired"
        val path = File(Environment.getExternalStorageDirectory(), "screenshots")
        path.mkdir()
        val file = File(path, "$fileName.png")
        file.createNewFile()
        return file
    }
}