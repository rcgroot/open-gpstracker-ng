/*------------------------------------------------------------------------------
 **    Author: René de Groot
 ** Copyright: (c) 2016 René de Groot All Rights Reserved.
 **------------------------------------------------------------------------------
 ** No part of this file may be reproduced
 ** or transmitted in any form or by any
 ** means, electronic or mechanical, for the
 ** purpose, without the express written
 ** permission of the copyright holder.
 *------------------------------------------------------------------------------
 *
 *   This file is part of "Open GPS Tracker - Exporter".
 *
 *   "Open GPS Tracker - Exporter" is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   "Open GPS Tracker - Exporter" is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with "Open GPS Tracker - Exporter".  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package nl.sogeti.android.gpstracker.ng.about

import android.app.Dialog
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import nl.sogeti.android.gpstracker.v2.R
import nl.sogeti.android.gpstracker.v2.databinding.FragmentAboutBinding

/**
 * Show a little HTML with licenses and version info
 */
class AboutFragment : DialogFragment() {

    companion object {
        val TAG = "AboutFragmentFragmentTag"
    }
    internal val model = AboutModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        var binding = DataBindingUtil.inflate<FragmentAboutBinding>(activity.layoutInflater, R.layout.fragment_about, null, false)
        binding.model = model

        val builder = AlertDialog.Builder(activity)
        builder.setView(binding.root)
        builder.setPositiveButton(android.R.string.ok) { _, _ -> dismiss() }

        return builder.create()
    }
}