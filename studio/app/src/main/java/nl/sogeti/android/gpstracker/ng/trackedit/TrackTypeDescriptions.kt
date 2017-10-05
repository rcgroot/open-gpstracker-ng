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
package nl.sogeti.android.gpstracker.ng.trackedit

import android.content.Context
import android.net.Uri
import nl.sogeti.android.gpstracker.integration.ContentConstants
import nl.sogeti.android.gpstracker.ng.utils.apply
import nl.sogeti.android.gpstracker.ng.utils.getString
import nl.sogeti.android.gpstracker.ng.utils.metaDataTrackUri
import nl.sogeti.android.gpstracker.ng.utils.updateCreateMetaData
import nl.sogeti.android.gpstracker.v2.R

const val KEY_META_FIELD_TRACK_TYPE = "SUMMARY_TYPE"
const val VALUE_TYPE_DEFAULT = "TYPE_DEFAULT"
const val VALUE_TYPE_BIKE = "TYPE_BIKE"
const val VALUE_TYPE_BOAT = "TYPE_BOAT"
const val VALUE_TYPE_CAR = "TYPE_CAR"
const val VALUE_TYPE_RUN = "TYPE_RUN"
const val VALUE_TYPE_WALK = "TYPE_WALK"
const val VALUE_TYPE_TRAIN = "TYPE_TRAIN"

class TrackTypeDescriptions {

    companion object {
        val defaultType = TrackType(R.drawable.ic_track_type_default, R.string.track_type_default, VALUE_TYPE_DEFAULT)

        val allTrackTypes by lazy {
            listOf(
                    defaultType,
                    TrackType(R.drawable.ic_track_type_walk, R.string.track_type_walk, VALUE_TYPE_WALK),
                    TrackType(R.drawable.ic_track_type_run, R.string.track_type_run, VALUE_TYPE_RUN),
                    TrackType(R.drawable.ic_track_type_bike, R.string.track_type_bike, VALUE_TYPE_BIKE),
                    TrackType(R.drawable.ic_track_type_car, R.string.track_type_car, VALUE_TYPE_CAR),
                    TrackType(R.drawable.ic_track_type_train, R.string.track_type_train, VALUE_TYPE_TRAIN),
                    TrackType(R.drawable.ic_track_type_boat, R.string.track_type_boat, VALUE_TYPE_BOAT)
            )
        }
    }


    fun trackTypeForContentType(contentType: String?): TrackType {
        val trackType = allTrackTypes.find { it.contentValue == contentType }

        return trackType ?: defaultType
    }

    fun loadTrackType(context: Context, trackUri: Uri): TrackType {
        val trackId: Long = trackUri.lastPathSegment.toLong()
        val typeSelection = Pair("${ContentConstants.MetaDataColumns.KEY} = ?", listOf(KEY_META_FIELD_TRACK_TYPE))
        val contentType = metaDataTrackUri(trackId).apply(context, selectionPair = typeSelection) { it.getString(ContentConstants.MetaDataColumns.VALUE) }

        return trackTypeForContentType(contentType)
    }

    fun saveTrackType(context: Context, trackUri: Uri, trackType: TrackType) {
        saveTrackType(context, trackUri, trackType.contentValue)
    }

    fun saveTrackType(context: Context, trackUri: Uri, trackType: String) {
        val trackId: Long = trackUri.lastPathSegment.toLong()
        metaDataTrackUri(trackId).updateCreateMetaData(context, KEY_META_FIELD_TRACK_TYPE, trackType)
    }

    data class TrackType(val drawableId: Int, val stringId: Int, val contentValue: String)
}
