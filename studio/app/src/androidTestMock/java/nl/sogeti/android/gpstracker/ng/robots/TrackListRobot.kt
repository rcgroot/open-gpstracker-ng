package nl.sogeti.android.gpstracker.ng.robots


import android.view.View
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import androidx.test.espresso.matcher.ViewMatchers.*
import nl.sogeti.android.gpstracker.ng.features.tracklist.TrackListViewAdapter
import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf

class TrackListRobot : Robot<TrackListRobot>("TrackList") {

    fun openRowContextMenu(rowNumber: Int): TrackListRobot {
        isTrackListDisplayed()
                .perform(scrollToPosition<TrackListViewAdapter.ViewHolder>(rowNumber))
                .perform(clickSubView(R.id.row_track_overflow))


        return this
    }

    fun share(): TrackListRobot {
        onView(allOf(withId(R.id.row_track_share), isDisplayed()))
                .perform(click())
        return this
    }

    fun edit(): TrackListRobot {
        onView(allOf(withId(R.id.row_track_edit), isDisplayed()))
                .perform(click())
        return this
    }

    fun delete(): TrackListRobot {
        onView(allOf(withId(R.id.row_track_delete), isDisplayed()))
                .perform(click())
        return this
    }

    fun cancelEdit(): TrackListRobot {
        onView(withId(R.id.fragment_trackEdit_ok))
                .perform(click())

        return this
    }

    fun cancelDelete(): TrackListRobot {
        onView(withId(R.id.fragment_trackdelete_cancel))
                .perform(click())

        return this
    }

    fun isTrackListDisplayed(): ViewInteraction {
        return onView(withId(R.id.fragment_tracklist_list))
                .check(matches(isCompletelyDisplayed()))
    }

    private fun clickSubView(subViewId: Int): ViewAction {
        return object : ViewAction {
            val click = ViewActions.click()

            override fun getDescription(): String {
                return click.description
            }

            override fun getConstraints(): Matcher<View> {
                return click.constraints
            }

            override fun perform(uiController: UiController?, view: View?) {
                click.perform(uiController, view?.findViewById(subViewId))
            }
        }
    }
}