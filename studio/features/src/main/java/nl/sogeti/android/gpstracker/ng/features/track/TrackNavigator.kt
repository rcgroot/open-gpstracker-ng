package nl.sogeti.android.gpstracker.ng.features.track

import android.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import android.net.Uri
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.View
import android.view.ViewGroup
import nl.sogeti.android.gpstracker.ng.features.about.AboutFragment
import nl.sogeti.android.gpstracker.ng.features.FeatureConfiguration
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphsActivity
import nl.sogeti.android.gpstracker.ng.features.graphs.GraphsFragment
import nl.sogeti.android.gpstracker.ng.features.trackedit.TrackEditDialogFragment
import nl.sogeti.android.gpstracker.ng.features.tracklist.TrackListFragment
import nl.sogeti.android.gpstracker.ng.features.tracklist.TrackListActivity
import nl.sogeti.android.opengpstrack.ng.features.R
import timber.log.Timber

class TrackNavigator(val activity: FragmentActivity){

    init {
        FeatureConfiguration.featureComponent.inject(this)
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

    companion object {

        private const val TAG_DIALOG = "DIALOG"
        private const val TRANSACTION_TRACKS = "FRAGMENT_TRANSACTION_TRACKS"
        private const val TRANSACTION_GRAPHS = "FRAGMENT_TRANSACTION_GRAPGS"

    }
}
