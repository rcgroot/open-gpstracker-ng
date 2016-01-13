/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2016 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.control;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.LinearLayout;

import nl.sogeti.android.gpstracker.data.Track;
import nl.sogeti.android.gpstracker.v2.R;

import static nl.sogeti.android.gpstracker.integration.ExternalConstants.STATE_LOGGING;
import static nl.sogeti.android.gpstracker.integration.ExternalConstants.STATE_PAUSED;
import static nl.sogeti.android.gpstracker.integration.ExternalConstants.STATE_STOPPED;

public class ControlHandler {

    private Listener listener;
    private final Track track;

    public ControlHandler(Listener listener, Track track) {
        this.listener = listener;
        this.track = track;
    }

    @BindingAdapter({"bind:state"})
    public static void setState(LinearLayout container, int state) {
        FloatingActionButton left = (FloatingActionButton) container.getChildAt(0);
        FloatingActionButton right = (FloatingActionButton) container.getChildAt(1);
        if (state == STATE_STOPPED) {
            left.setVisibility(View.GONE);
            right.setImageResource(R.drawable.ic_navigation_black_24dp);
        } else if (state == STATE_LOGGING) {
            left.setVisibility(View.VISIBLE);
            left.setImageResource(R.drawable.ic_stop_black_24dp);
            startAnimation(right);
        } else if (state == STATE_PAUSED) {
            left.setVisibility(View.VISIBLE);
            left.setImageResource(R.drawable.ic_stop_black_24dp);
            right.setImageResource(R.drawable.ic_navigation_black_24dp);
        }
    }

    public void onClickLeft(View view) {
        if (track.getState() == STATE_LOGGING) {
            listener.stopLogging();
        } else if (track.getState() == STATE_PAUSED) {
            listener.stopLogging();
        }
    }

    public void onClickRight(View view) {
        if (track.getState() == STATE_STOPPED) {
            listener.startLogging();
        } else if (track.getState() == STATE_LOGGING) {
            listener.pauseLogging();
        } else if (track.getState() == STATE_PAUSED) {
            listener.resumeLogging();
        }
    }

    interface Listener {
        void startLogging();

        void stopLogging();

        void pauseLogging();

        void resumeLogging();
    }


    private static void startAnimation(FloatingActionButton imageView) {
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof Animatable) {
            ((Animatable) drawable).start();
        }
    }
}
