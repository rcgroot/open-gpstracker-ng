package nl.sogeti.android.gpstracker.ng.robots

import android.app.Activity

class TrackListRobot(private val activity: Activity) : Robot<TrackListRobot>("TrackList") {
    fun openRowContextMenu(rowNumber: Int): TrackListRobot {
        return this
    }

}