/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
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
package nl.sogeti.android.gpstracker.ng.utils

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri

/* **
 * Extensions for dealing with the data in content providers
 * **/

fun Cursor.getString(columnName: String): String {
    val index = this.getColumnIndex(columnName)
    val value = this.getString(index)

    return value ?: ""
}

fun Cursor.getLong(columnName: String): Long {
    val index = this.getColumnIndex(columnName)
    val value = this.getLong(index)

    return value
}

fun <T> Uri.map(context: Context,
                operation: (Cursor) -> T,
                projection: List<String>? = null,
                selection: Pair <String, List<String>>? = null): List<T> {
    val result = mutableListOf<T>()

    this.apply(context, {
        do {
            result.add(operation(it))
        } while (it.moveToNext())
    }, projection, selection)

    return result
}

fun <T> Uri.apply(context: Context,
                  operation: (Cursor) -> T,
                  projection: List<String>? = null,
                  selection: Pair <String, List<String>>? = null): T? {
    val selectionArgs = selection?.second?.toTypedArray()
    val selection = selection?.first
    var result: T? = null
    var cursor: Cursor? = null
    try {
        cursor = context.contentResolver.query(this, projection?.toTypedArray(), selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            result = operation(cursor)
        }
    } finally {
        cursor?.close()
    }

    return result
}

fun Uri.append(path: String): Uri {
    return Uri.withAppendedPath(this, path)
}

fun Uri.append(id: Long): Uri {
    return ContentUris.withAppendedId(this, id)
}