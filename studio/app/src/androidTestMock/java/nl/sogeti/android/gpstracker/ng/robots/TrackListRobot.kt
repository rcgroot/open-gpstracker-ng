package nl.sogeti.android.gpstracker.ng.robots

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.UiController
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.contrib.RecyclerViewActions.scrollToPosition
import android.support.test.espresso.matcher.ViewMatchers.*
import android.view.View
import nl.sogeti.android.gpstracker.ng.tracklist.TrackListViewAdapter
import nl.sogeti.android.gpstracker.v2.R
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.anyOf

class TrackListRobot : Robot<TrackListRobot>("TrackList") {

    fun openRowContextMenu(rowNumber: Int): TrackListRobot {

        onView(matchTrackList())
                .check(matches(isDisplayed()))
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

    fun isTrackListDisplayed() {
        onView(matchTrackList())
                .check(matches(isCompletelyDisplayed()))
    }

    /**
     * Matches the recycle view id, which might change when it is included through xml as it is the root view element
     */
    private fun matchTrackList() = anyOf(withId(R.id.fragment_tracklist), withId(R.id.fragment_tracklist_list))

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