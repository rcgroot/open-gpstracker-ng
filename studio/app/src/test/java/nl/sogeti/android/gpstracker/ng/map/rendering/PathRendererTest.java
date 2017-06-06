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
package nl.sogeti.android.gpstracker.ng.map.rendering;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.google.android.gms.maps.model.LatLng;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.anyFloat;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PathRendererTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    Canvas canvas;
    private PathRenderer sut;

    @Before
    public void setup() {
        sut = new PathRenderer(128, 6, null, null, null);
    }

    @Test
    public void initCreateCorrectProjection() {
        Assert.assertEquals(128.0, sut.getProjection().getTileSize(), 0.00000001);
    }

    @Test
    public void singlePointPath() {
        // Prepare
        Bitmap start = mock(Bitmap.class);
        Bitmap stop = mock(Bitmap.class);
        List<List<LatLng>> latLng = new ArrayList<>();
        List<LatLng> segment = new ArrayList<>();
        latLng.add(segment);
        segment.add(new LatLng(52.0, 5.0));
        sut = new PathRenderer(32.0F, 2.0F, latLng, start, stop);
        Paint paint = mock(Paint.class);
        Path path = mock(Path.class);

        // Execute
        sut.drawPath(canvas, 0, 0, 0, paint, path);

        // Verify
        verify(canvas).drawBitmap(eq(start), anyFloat(), anyFloat(), (Paint) anyObject());
        verify(canvas).drawPath(eq(path), eq(paint));
    }
}
