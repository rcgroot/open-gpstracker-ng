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
import nl.sogeti.android.gpstracker.ng.robots.AboutRobot
import nl.sogeti.android.gpstracker.ng.robots.TrackListRobot
import nl.sogeti.android.gpstracker.ng.robots.TrackRobot
import nl.sogeti.android.gpstracker.ng.track.TrackActivity
import org.junit.Rule
import org.junit.Test

class TourScreenshots {

    @get:Rule
    var activityRule = ActivityTestRule<TrackActivity>(TrackActivity::class.java)

    @Test
    fun tour() {
        val trackRobot = TrackRobot(activityRule.activity)
        val aboutRobot = AboutRobot(activityRule.activity)
        val trackListRobot = TrackListRobot(activityRule.activity)
        trackRobot
                .start().takeScreenShot()
                .editTrack().takeScreenShot()
                .openTrackTypeSpinner().takeScreenShot()
                .selectWalking()
                .ok()
                .startRecording().takeScreenShot()
                .sleep(10)
                .pauseRecording().takeScreenShot()
                .resumeRecording().takeScreenShot()
                .sleep(10)
                .stopRecording().takeScreenShot()
                .openAbout()
        aboutRobot
                .start().takeScreenShot()
                .ok()
        trackRobot
                .openTrackList().takeScreenShot()
        trackListRobot
                .openRowContextMenu(1).takeScreenShot()

        trackRobot.stop()
        aboutRobot.stop()
    }
}