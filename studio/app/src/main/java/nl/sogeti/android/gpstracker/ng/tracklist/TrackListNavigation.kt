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
import nl.sogeti.android.gpstracker.ng.gpxexport.MIME_TYPE_GPX
import nl.sogeti.android.gpstracker.ng.track.TrackActivity
import nl.sogeti.android.gpstracker.ng.trackdelete.TrackDeleteDialogFragment
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
            if (fragment is ActivityResultLambda) {
                fragment.startActivityForResult(intent, param)
            }
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
            if (fragment is ActivityResultLambda) {
                fragment.startActivityForResult(intent, param)
            }
        }
        else {
            AlertDialog.Builder(fragment.context)
                    .setTitle("Not implemented ")
                    .setMessage("This feature does not exist pre-Lollipop")
                    .create()
                    .show()
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
