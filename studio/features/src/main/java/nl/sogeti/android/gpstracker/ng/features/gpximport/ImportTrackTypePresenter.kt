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
package nl.sogeti.android.gpstracker.ng.features.gpximport

import android.view.View
import android.widget.AdapterView
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions
import nl.sogeti.android.gpstracker.ng.features.util.AbstractPresenter

class ImportTrackTypePresenter : AbstractPresenter() {

    val model = ImportTrackTypeModel()

    var resultLambda: (String) -> Unit = {}

    val onItemSelectedListener: AdapterView.OnItemSelectedListener by lazy {
        object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                model.selectedPosition.set(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                model.selectedPosition.set(AdapterView.INVALID_POSITION)
            }
        }
    }

    fun ok() {
        model.dismiss.set(true)
        val i = model.selectedPosition.get()
        val trackType = if (i == AdapterView.INVALID_POSITION) {
            TrackTypeDescriptions.allTrackTypes[0].contentValue
        } else {
            TrackTypeDescriptions.allTrackTypes[i].contentValue
        }
        resultLambda(trackType)
    }

    fun cancel() {
        model.dismiss.set(true)
    }
}
