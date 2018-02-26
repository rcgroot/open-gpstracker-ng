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
package nl.sogeti.android.gpstracker.ng.features.tracklist

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.gpxexport.GpxShareProvider.Companion.MIME_TYPE_GENERAL
import nl.sogeti.android.gpstracker.ng.features.gpximport.ImportTrackTypeDialogFragment
import nl.sogeti.android.gpstracker.ng.features.track.TrackActivity
import nl.sogeti.android.gpstracker.ng.features.trackdelete.TrackDeleteDialogFragment
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackEditDialogFragment
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackTypeDescriptions.Companion.KEY_META_FIELD_TRACK_TYPE
import nl.sogeti.android.gpstracker.utils.ActivityResultLambda
import nl.sogeti.android.gpstracker.utils.VersionHelper
import nl.sogeti.android.opengpstrack.ng.features.R
import javax.inject.Inject

class TrackListNavigation(val fragment: Fragment) {

    @Inject
    lateinit var packageManager: PackageManager
    @Inject
    lateinit var versionHelper: VersionHelper

    init {
        FeatureConfiguration.featureComponent.inject(this)
    }

    fun showTrackDeleteDialog(track: Uri) {
        TrackDeleteDialogFragment.newInstance(track)
                .show(fragment.fragmentManager, TAG_DIALOG)
    }

    fun showTrackEditDialog(trackUri: Uri) {
        TrackEditDialogFragment.newInstance(trackUri)
                .show(fragment.fragmentManager, TAG_DIALOG)
    }

    fun showIntentChooser(intent: Intent, @StringRes text: Int) {
        fragment.startActivity(Intent.createChooser(intent, fragment.getString(text)))
    }

    fun finishTrackSelection() {
        if (fragment.activity is TrackActivity) {
            // For tablet we'll opt to leave the track list on the screen instead of removing it
            // getSupportFragmentManager().popBackStack(TRANSACTION_TRACKS, POP_BACK_STACK_INCLUSIVE);
        } else {
            fragment.activity?.finish()
        }
    }

    fun startGpxFileSelection(param: (Intent?) -> Unit) {
        if (versionHelper.isAtLeast(Build.VERSION_CODES.KITKAT)) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = MIME_TYPE_GENERAL
            startTypeImport(intent, param)
        } else {
            val context = fragment.context
                    ?: throw IllegalStateException("Attempting to run select file outside lifecycle of fragment")
            AlertDialog.Builder(context)
                    .setTitle("Not implemented ")
                    .setMessage("This feature does not exist pre-KitKat")
                    .create()
                    .show()
        }
    }

    fun startGpxDirectorySelection(param: (Intent?) -> Unit) {
        if (versionHelper.isAtLeast(Build.VERSION_CODES.LOLLIPOP)) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            startTypeImport(intent, param)
        } else {
            val context = fragment.context
                    ?: throw IllegalStateException("Attempting to run select directory outside lifecycle of fragment")
            AlertDialog.Builder(context)
                    .setTitle("Not implemented ")
                    .setMessage("This feature does not exist pre-Lollipop")
                    .create()
                    .show()
        }
    }

    private fun startTypeImport(intent: Intent, param: (Intent?) -> Unit) {
        if (fragment is ActivityResultLambda) {
            fragment.startActivityForResult(intent) { resultIntent ->
                resultIntent?.let {
                    val fragmentManager = fragment.fragmentManager
                            ?: throw IllegalStateException("Attempting to run import outside lifecycle of fragment")
                    ImportTrackTypeDialogFragment().show(fragmentManager, TAG_DIALOG) { type ->
                        resultIntent.putExtra(KEY_META_FIELD_TRACK_TYPE, type)
                        param(resultIntent)
                    }
                }
            }
        }
    }

    fun showInstallHintForOGTExporterApp() {
        val context = fragment.context ?: return
        AlertDialog.Builder(context)
                .setTitle(R.string.fragment_tracks_exporter_title)
                .setMessage(R.string.fragment_tracks_exporter_body)
                .setNegativeButton(android.R.string.cancel, { dialog, _ -> dialog.dismiss() })
                .setPositiveButton(R.string.permission_button_install, { _, _ ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$OGT_EXPORTER_PACKAGE_NAME"))
                    context.startActivity(intent)
                })
                .show()
    }

    fun openExternalOGTExporterApp() {
        val intent = packageManager.getLaunchIntentForPackage(OGT_EXPORTER_PACKAGE_NAME)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        fragment.startActivity(intent)
    }

    companion object {

        private const val TAG_DIALOG = "DIALOG"

    }
}
