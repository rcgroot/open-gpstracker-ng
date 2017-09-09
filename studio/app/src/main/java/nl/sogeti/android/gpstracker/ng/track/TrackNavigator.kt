package nl.sogeti.android.gpstracker.ng.track

import android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.about.AboutFragment
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication
import nl.sogeti.android.gpstracker.ng.common.abstractpresenters.Navigation
import nl.sogeti.android.gpstracker.ng.common.controllers.packagemanager.PackageManagerFactory
import nl.sogeti.android.gpstracker.ng.graphs.GraphsActivity
import nl.sogeti.android.gpstracker.ng.graphs.GraphsFragment
import nl.sogeti.android.gpstracker.ng.recording.GPS_STATUS_PACKAGE_NAME
import nl.sogeti.android.gpstracker.ng.trackdelete.TrackDeleteDialogFragment
import nl.sogeti.android.gpstracker.ng.trackedit.TrackEditDialogFragment
import nl.sogeti.android.gpstracker.ng.tracklist.OGT_EXPORTER_PACKAGE_NAME
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListActivity
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListFragment
import nl.sogeti.android.gpstracker.v2.R
import timber.log.Timber
import javax.inject.Inject

private const val TAG_DIALOG = "DIALOG"
private const val TRANSACTION_TRACKS = "FRAGMENT_TRANSACTION_TRACKS"
private const val TRANSACTION_GRAPHS = "FRAGMENT_TRANSACTION_GRAPGS"

class TrackNavigator(val activity: FragmentActivity): Navigation {

    @Inject
    lateinit var packageManagerFactory: PackageManagerFactory

    init {
        GpsTrackerApplication.appComponent.inject(this)
    }

    fun showAboutDialog() {
        AboutFragment().show(activity.supportFragmentManager, AboutFragment.TAG)
    }

    fun showTrackEditDialog(trackUri: Uri) {
        TrackEditDialogFragment.newInstance(trackUri).show(activity.supportFragmentManager, TAG_DIALOG)
    }

    fun showGraphs() {
        if (hasLeftContainer()) {
            toggleContainerFragment(GraphsFragment.newInstance(), TRANSACTION_GRAPHS)
        } else {
            GraphsActivity.start(activity)
        }
    }

    fun showTrackSelection() {
        if (hasLeftContainer()) {
            toggleContainerFragment(TrackListFragment.newInstance(), TRANSACTION_TRACKS)
        } else {
            TrackListActivity.start(activity)
        }
    }

    fun hideTrackList() {
        if (hasLeftContainer()) {
            // For tablet we'll opt to leave the track list on the screen instead of removing it
            // getSupportFragmentManager().popBackStack(TRANSACTION_TRACKS, POP_BACK_STACK_INCLUSIVE);
        } else {
            activity.finish()
        }
    }

    fun showTrackDeleteDialog(track: Uri) {
        TrackDeleteDialogFragment.newInstance(track).show(activity.supportFragmentManager, TAG_DIALOG)
    }

    fun showIntentChooser(intent: Intent, text: CharSequence) {
        activity.startActivity(Intent.createChooser(intent, text))
    }

    private fun hasLeftContainer(): Boolean {
        val leftContainer = activity.findViewById<View>(R.id.track_leftcontainer)
        return leftContainer != null && leftContainer is ViewGroup
    }


    private fun toggleContainerFragment(goal: Fragment, tag: String) {
        val fragment = activity.supportFragmentManager.findFragmentById(R.id.track_leftcontainer)
        if (fragment != null) {
            if (fragment is TrackListFragment) {
                activity.supportFragmentManager.popBackStack(TRANSACTION_TRACKS, POP_BACK_STACK_INCLUSIVE)
            } else if (fragment is GraphsFragment) {
                activity.supportFragmentManager.popBackStack(TRANSACTION_GRAPHS, POP_BACK_STACK_INCLUSIVE)
            }
        }
        if (fragment == null || fragment.javaClass != goal.javaClass) {
            replaceFragmentInLeftContainer(goal, tag)
        }
    }

    private fun replaceFragmentInLeftContainer(goal: Fragment, tag: String) {
        try {
            activity.supportFragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_left,
                            R.anim.enter_from_left, R.anim.exit_to_left)
                    .addToBackStack(tag)
                    .replace(R.id.track_leftcontainer, goal)
                    .commit()
        } catch (e: Exception) {
            Timber.e(e, "Transaction to add Fragment failed")
        }
    }

    fun startGpxFileSelection() {
        AlertDialog.Builder(activity)
                .setTitle("Mot implemented")
                .setMessage("this feature does not exist yet")
                .create()
                .show()
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
        val intent = packageManager.getLaunchIntentForPackage(OGT_EXPORTER_PACKAGE_NAME);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        context.startActivity(intent);
    }
}
