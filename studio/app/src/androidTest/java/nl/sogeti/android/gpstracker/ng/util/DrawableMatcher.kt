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
package nl.sogeti.android.gpstracker.ng.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.res.ResourcesCompat.getDrawable
import android.view.View
import android.widget.ImageView
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import timber.log.Timber

class DrawableMatcher(val id: Int) : TypeSafeMatcher<View>() {

    override fun describeTo(description: Description?) {
        description?.appendText("has matching drawable resource")
    }

    override fun matchesSafely(item: View?): Boolean {
        var matches = false
        if (item is ImageView) {
            val expectedDrawable = getDrawable(item.resources, id, null)
            if (expectedDrawable != null) {
                val expectedBitmap = renderDrawable(expectedDrawable)

                val itemBitmap: Bitmap
                val itemDrawable = item.drawable
                if (itemDrawable is BitmapDrawable) {
                    itemBitmap = itemDrawable.bitmap
                } else {
                    itemBitmap = renderDrawable(itemDrawable)
                }
                matches = itemBitmap.sameAs(expectedBitmap)
            }
        }

        return matches
    }

    private fun renderDrawable(itemDrawable: Drawable): Bitmap {
        val itemBitmap = Bitmap.createBitmap(itemDrawable.intrinsicWidth, itemDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888);
        val canvas = Canvas(itemBitmap)
        itemDrawable.setBounds(0, 0, canvas.width, canvas.height)
        itemDrawable.draw(canvas)

        return itemBitmap
    }
}