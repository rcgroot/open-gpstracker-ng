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
import android.provider.BaseColumns
import timber.log.Timber

/* **
 * Extensions for dealing with the data in content providers
 * **/

/**
 * Read a single String value from a cursor using the columns name
 *
 * @param columnName name of the column
 *
 * @return null if the column doesn't exist or stores a null value
 */
fun Cursor.getString(columnName: String): String? {
    return this.applyGetter(columnName, Cursor::getString)
}

/**
 * Read a single Long value from a cursor using the columns name
 *
 * @param columnName name of the column
 *
 * @return null if the column doesn't exist or stores a null value
 */
fun Cursor.getLong(columnName: String): Long? {
    return this.applyGetter(columnName, Cursor::getLong)
}

/**
 * Read a single Double value from a cursor using the columns name
 *
 * @param columnName name of the column
 *
 * @return null if the column doesn't exist or stores a null value
 */
fun Cursor.getDouble(columnName: String): Double? {
    return this.applyGetter(columnName, Cursor::getDouble)
}

/**
 * Read a single Int value from a cursor using the columns name
 *
 * @param columnName name of the column
 *
 * @return null if the column doesn't exist or stores a null value
 */
fun Cursor.getInt(columnName: String): Int? {
    return this.applyGetter(columnName, Cursor::getInt)
}

private fun <T> Cursor.applyGetter(columnName: String, getter: (Cursor, Int) -> T): T? {
    val index = this.getColumnIndex(columnName)
    val value: T?
    if (index >= 0) {
        value = getter(this, index)
    } else {
        value = null
    }

    return value
}

/**
 * Apply a single operation to a cursor on the all items in a Uri
 * to build a list
 *
 * @param context context through which to access the resources
 * @param operation the operation to execute
 * @param projection optional projection
 * @param selection optional selection, query string with parameter arguments listed
 *
 * @return List of T consisting of operation results, empty when there are no rows
 */
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

/**
 * Apply a single operation to a cursor on the first row of a Uri
 *
 * @param context context through which to access the resources
 * @param operation the operation to execute
 * @param projection optional projection
 * @param selectionPair optional selection
 *
 * @return Null or T when the operation was applied to the first row of the cursor
 */
fun <T> Uri.apply(context: Context,
                  operation: (Cursor) -> T?,
                  projection: List<String>? = null,
                  selectionPair: Pair <String, List<String>>? = null): T? {
    val selectionArgs = selectionPair?.second?.toTypedArray()
    val selection = selectionPair?.first
    var result: T? = null
    var cursor: Cursor? = null
    try {
        cursor = context.contentResolver.query(this, projection?.toTypedArray(), selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            result = operation(cursor)
        } else {
            Timber.w("Uri $this apply operation didn't have results")
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

fun Uri.count(context: Context, projection: List<String>? = null,
              selectionPair: Pair <String, List<String>>? = null) : Int {
    val selectionArgs = selectionPair?.second?.toTypedArray()
    val selection = selectionPair?.first
    var result = 0
    var cursor: Cursor? = null
    try {
        cursor = context.contentResolver.query(this, projection?.toTypedArray(), selection, selectionArgs, null)
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.count
        } else {
            Timber.w("Uri $this apply operation didn't have results")
        }
    } finally {
        cursor?.close()
    }

    return result
}