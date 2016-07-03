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

import junit.framework.Assert;

import org.junit.Test;

public class PointTest {

    @Test
    public void testDistancePositiveRight() {
        // Setup
        Point a = new Point(10, 10);
        Point b = new Point(12, 12);

        // Execute
        double distanceSquared = a.squaredDistanceTo(b);

        // Verify
        Assert.assertEquals(4, distanceSquared, 0.000001);
    }

    @Test
    public void testDistancePositiveLeft() {
        // Setup
        Point a = new Point(10, 10);
        Point b = new Point(12, 12);

        // Execute
        double distanceSquared = b.squaredDistanceTo(a);

        // Verify
        Assert.assertEquals(4, distanceSquared, 0.000001);
    }

    @Test
    public void testDistanceNegativeRight() {
        // Setup
        Point a = new Point(-1, -1);
        Point b = new Point(1, 1);

        // Execute
        double distanceSquared = a.squaredDistanceTo(b);

        // Verify
        Assert.assertEquals(4, distanceSquared, 0.000001);
    }

    @Test
    public void testDistanceNegativeLeft() {
        // Setup
        Point a = new Point(1, 1);
        Point b = new Point(-1, -1);

        // Execute
        double distanceSquared = b.squaredDistanceTo(a);

        // Verify
        Assert.assertEquals(4, distanceSquared, 0.000001);
    }
}
