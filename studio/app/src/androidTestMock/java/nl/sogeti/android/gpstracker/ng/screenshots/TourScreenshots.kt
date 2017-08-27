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
package nl.sogeti.android.gpstracker.ng.screenshots

import android.support.test.rule.ActivityTestRule
import nl.sogeti.android.gpstracker.ng.robots.*
import nl.sogeti.android.gpstracker.ng.track.TrackActivity
import org.junit.*

class TourScreenshots {

    @get:Rule
    var activityRule = ActivityTestRule<TrackActivity>(TrackActivity::class.java)

    private lateinit var trackRobot: TrackRobot
    private lateinit var aboutRobot: AboutRobot
    private lateinit var trackListRobot: TrackListRobot
    private lateinit var graphRobot: GraphRobot

    companion object {
        @BeforeClass
        @JvmStatic
        fun setupOnce() {
            Robot.resetScreenShots()
        }
    }

    @Before
    fun setUp() {
        trackRobot = TrackRobot(activityRule.activity)
        aboutRobot = AboutRobot(activityRule.activity)
        trackListRobot = TrackListRobot()
        graphRobot = GraphRobot()

    }

    @After
    fun tearDown() {
        trackRobot.stop()
        aboutRobot.stop()
    }

    @Test
    fun recordTrack() {
        trackRobot
                .start()
                .startRecording().takeScreenShot()
                .sleep(10)
                .pauseRecording().takeScreenShot()
                .resumeRecording().takeScreenShot()
                .sleep(10)
                .stopRecording().takeScreenShot()
    }

    @Test
    fun editTrack() {
        trackRobot
                .start()
                .takeScreenShot()
                .editTrack().takeScreenShot()
                .openTrackTypeSpinner().takeScreenShot()
                .selectWalking()
                .ok()
    }

    @Test
    fun trackList() {
        trackRobot
                .start()
                .openTrackList().takeScreenShot()
        trackListRobot
                .openRowContextMenu(0).takeScreenShot()
                .share().takeScreenShot()
                .back()
                .openRowContextMenu(0)
                .edit().takeScreenShot()
                .back()
                .openRowContextMenu(0)
                .delete().takeScreenShot()
                .back()
    }

    @Test
    fun about() {
        trackRobot
                .start()
                .openAbout().takeScreenShot()
        aboutRobot
                .start()
                .ok()
    }

    @Test
    fun graph() {
        trackRobot
                .start()
                .openGraph()
        graphRobot
                .takeScreenShot()
                .back()

    }
}