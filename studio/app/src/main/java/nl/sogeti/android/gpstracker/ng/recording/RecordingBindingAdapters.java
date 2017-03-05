/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: René de Groot
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
package nl.sogeti.android.gpstracker.ng.recording;

import android.databinding.BindingAdapter;
import android.support.v7.content.res.AppCompatResources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import nl.sogeti.android.gpstracker.ng.common.bindings.CommonBindingAdapters;
import nl.sogeti.android.gpstracker.v2.R;

public class RecordingBindingAdapters extends CommonBindingAdapters {

    @BindingAdapter("isRecording")
    public void setRecording(final ViewGroup container, boolean isRecording) {
        if (isRecording) {
            container.setVisibility(View.VISIBLE);
            container.animate().translationY(0).start();
        } else {
            container.animate().translationY(-container.getHeight()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    container.setVisibility(View.GONE);
                }
            }).start();
        }
    }

    @BindingAdapter(("gps_signal_quality"))
    public void setGpsSignalQuality(final ImageView imageView, int quality) {
        switch (quality) {
            case 0:
                imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.getContext(), R.drawable.ic_satellite_none));
                break;
            case 1:
                imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.getContext(), R.drawable.ic_satellite_low));
                break;
            case 2:
                imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.getContext(), R.drawable.ic_satellite_medium));
                break;
            case 3:
                imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.getContext(), R.drawable.ic_satellite_high));
                break;
            case 4:
                imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.getContext(), R.drawable.ic_satellite_full));
                break;
            default:
                imageView.setImageDrawable(AppCompatResources.getDrawable(imageView.getContext(), R.drawable.ic_satellite_none));
                break;
        }
    }
}