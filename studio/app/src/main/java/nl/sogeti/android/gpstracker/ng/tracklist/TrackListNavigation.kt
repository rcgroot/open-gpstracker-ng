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
package nl.sogeti.android.gpstracker.ng.tracklist

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.Navigation
import nl.sogeti.android.gpstracker.ng.common.controllers.packagemanager.PackageManagerFactory
import nl.sogeti.android.gpstracker.ng.gpxexport.MIME_TYPE_GENERAL
import nl.sogeti.android.gpstracker.ng.gpximport.ImportTrackTypeDialogFragment
import nl.sogeti.android.gpstracker.ng.track.TrackActivity
import nl.sogeti.android.gpstracker.ng.trackdelete.TrackDeleteDialogFragment
import nl.sogeti.android.gpstracker.ng.trackedit.KEY_META_FIELD_TRACK_TYPE
import nl.sogeti.android.gpstracker.ng.trackedit.TrackEditDialogFragment
import nl.sogeti.android.gpstracker.ng.utils.ActivityResultLambda
import nl.sogeti.android.gpstracker.ng.utils.VersionHelper
import nl.sogeti.android.gpstracker.v2.R
import javax.inject.Inject

private const val TAG_DIALOG = "DIALOG"

class TrackListNavigation(val fragment: Fragment) : Navigation {

    @Inject
    lateinit var packageManagerFactory: PackageManagerFactory
    @Inject
    lateinit var versionHelper: VersionHelper

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun showTrackDeleteDialog(track: Uri) {
        TrackDeleteDialogFragment.newInstance(track).show(fragment.fragmentManager, TAG_DIALOG)
    }

    fun showTrackEditDialog(trackUri: Uri) {
        TrackEditDialogFragment.newInstance(trackUri).show(fragment.fragmentManager, TAG_DIALOG)
    }

    fun showIntentChooser(intent: Intent, text: CharSequence) {
        fragment.startActivity(Intent.createChooser(intent, text))
    }

    fun finishTrackSelection() {
        if (fragment.activity is TrackActivity) {
            // For tablet we'll opt to leave the track list on the screen instead of removing it
            // getSupportFragmentManager().popBackStack(TRANSACTION_TRACKS, POP_BACK_STACK_INCLUSIVE);
        } else {
            fragment.activity.finish()
        }
    }

    fun startGpxFileSelection(param: (Intent?) -> Unit) {
        if (versionHelper.isAtLeast(Build.VERSION_CODES.KITKAT)) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = MIME_TYPE_GENERAL
            startTypeImport(intent, param)
        } else {
            AlertDialog.Builder(fragment.context)
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
            AlertDialog.Builder(fragment.context)
                    .setTitle("Not implemented ")
                    .setMessage("This feature does not exist pre-Lollipop")
                    .create()
                    .show()
        }
    }

    private fun startTypeImport(intent: Intent, param: (Intent?) -> Unit) {
        if (fragment is ActivityResultLambda) {
            fragment.startActivityForResult(intent) { intent ->
                intent?.let {
                    ImportTrackTypeDialogFragment().show(fragment.fragmentManager, TAG_DIALOG) { type ->
                        intent.putExtra(KEY_META_FIELD_TRACK_TYPE, type)
                        param(intent)
                    }
                }
            }
        }
    }

    fun showInstallHintForOGTExporterApp(context: Context) {
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

    fun openExternalOGTExporterApp(context: Context) {
        val packageManager = packageManagerFactory.createPackageManager(context)
        val intent = packageManager.getLaunchIntentForPackage(OGT_EXPORTER_PACKAGE_NAME)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        context.startActivity(intent)
    }
}
