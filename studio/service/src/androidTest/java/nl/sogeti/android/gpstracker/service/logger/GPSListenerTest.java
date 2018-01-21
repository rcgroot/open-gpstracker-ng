/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) 2015 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.service.logger;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.test.InstrumentationTestCase;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import nl.sogeti.android.gpstracker.integration.ContentConstants;
import timber.log.Timber;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GPSListenerTest extends InstrumentationTestCase {

    private LoggerNotification mockNotification;
    private GPSLoggerService mockService;
    private LoggerPersistence mockPersistence;
    private MockContentResolver mockResolver;
    private ContentProvider mockProvider;
    private Context mockApplicationContext;

    private GPSListener sut;
    private Location referenceLocation;
    private LocationManager mockLocationManager;
    private PowerManager mockPowerManager;

    @TargetApi(Build.VERSION_CODES.FROYO)
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockService = mock(GPSLoggerService.class);
        mockLocationManager = mock(LocationManager.class);
        when(mockService.getSystemService(Context.LOCATION_SERVICE)).thenReturn(mockLocationManager);
        mockResolver = new MockContentResolver();
        when(mockService.getContentResolver()).thenReturn(mockResolver);
        mockApplicationContext = mock(Context.class);
        when(mockService.getApplicationContext()).thenReturn(mockApplicationContext);
        mockProvider = new MyMockContentProvider();
        mockResolver.addProvider(ContentConstants.AUTHORITY, mockProvider);
        mockPersistence = mock(LoggerPersistence.class);
        mockPowerManager = mock(PowerManager.class);
        doNothing().when(mockPowerManager).updateWakeLock(anyInt());
        mockNotification = mock(LoggerNotification.class);
        doNothing().when(mockNotification).startLogging(anyInt(), anyInt(), anyBoolean(), anyLong());

        sut = new GPSListener(mockService, mockPersistence, mockNotification, mockPowerManager);

        referenceLocation = new Location("GPSListenerTest");
        referenceLocation.setLatitude(37.422006d);
        referenceLocation.setLongitude(-122.084095d);
        referenceLocation.setAccuracy(10f);
        referenceLocation.setAltitude(12.5d);
    }

    @Override
    protected void tearDown() throws Exception {
        sut = null;
        mockNotification = null;
        mockService = null;
        mockPersistence = null;

        super.tearDown();
    }

    @SmallTest
    public void testInaccurateLocation() {
        sut.onCreate();
        Location reference = new Location(this.referenceLocation);
        reference.setLatitude(reference.getLatitude() + 0.01d); //Other side of the golfpark, about 1100 meters
        sut.storeLocation(reference);
        referenceLocation.setAccuracy(50f);
        Assert.assertNull("An unacceptable fix", sut.locationFilter(this.referenceLocation));
    }

    @SmallTest
    public void testAccurateLocation() {
        sut.onCreate();
        Location reference = new Location(this.referenceLocation);
        reference.setLatitude(reference.getLatitude() + 0.01d); //Other side of the golfpark, about 1100 meters
        reference.setTime(reference.getTime() + 60000l); // In one minute times
        sut.storeLocation(reference);
        referenceLocation.setAccuracy(9f);
        Location returned = sut.locationFilter(this.referenceLocation);
        Assert.assertNotNull("An acceptable fix", returned);
    }

    @SmallTest
    public void testCloseLocation() {
        sut.onCreate();
        Location reference = new Location(this.referenceLocation);
        reference.setLatitude(reference.getLatitude() + 0.0001d); // About 11 meters
        reference.setTime(reference.getTime() + 6000l); // In 6 seconds times
        sut.storeLocation(reference);

        this.referenceLocation.setAccuracy(9f);
        Assert.assertNotNull("An acceptable fix", sut.locationFilter(this.referenceLocation));

        sut.stopLogging();
    }

    @SmallTest
    public void testBetterSomethingThenNothingAccurateLocation() {
        sut.onCreate();
        Location first = this.referenceLocation;
        first.setAccuracy(150f);

        Location second = new Location(this.referenceLocation);
        second.setAccuracy(100f);
        second.setLatitude(second.getLatitude() + 0.01d); //Other side of the golfpark, about 1100 meters
        second.setTime(second.getTime() + 60000l); // In one minute times

        Location third = new Location(this.referenceLocation);
        third.setAccuracy(125f);
        third.setLatitude(third.getLatitude() + 0.01d); //about 1100 meters
        third.setTime(third.getTime() + 60000l); // In one minute times

        Assert.assertNull("An unacceptable fix", sut.locationFilter(first));
        Assert.assertNull("An unacceptable fix", sut.locationFilter(second));
        Location last = sut.locationFilter(third);
        Assert.assertNotNull("An acceptable fix", last);
        Assert.assertEquals("Best one was the second one", second, last);
        Assert.assertNull("An unacceptable fix", sut.locationFilter(first));
    }

    @SmallTest
    public void testToFastLocation() {
        when(mockPersistence.isSpeedChecked()).thenReturn(true);
        sut.onCreate();
        Location reference = new Location(this.referenceLocation);
        reference.setLatitude(reference.getLatitude() + 0.0001d);
        reference.setTime(reference.getTime() + 6000l); // In 6 seconds times
        reference.setSpeed(419f);
        reference.setAccuracy(9f);
        sut.storeLocation(this.referenceLocation);

        Location sane = sut.locationFilter(reference);
        Assert.assertNotNull("Filter result", sane);
        Assert.assertFalse("No speed anymore", sane.hasSpeed());
        Assert.assertEquals("No speed", 0.0f, sane.getSpeed());
        Assert.assertSame("Still the same", reference, sane);

        sut.stopLogging();
    }

    @SmallTest
    public void testNormalSpeedLocation() {
        sut.onCreate();
        Location reference = new Location(this.referenceLocation);
        reference.setLatitude(reference.getLatitude() + 0.0001d);
        reference.setTime(reference.getTime() + 6000l); // In one minute times
        reference.setSpeed(4f);
        reference.setAccuracy(9f);
        sut.storeLocation(this.referenceLocation);

        Location sane = sut.locationFilter(reference);
        Assert.assertTrue("Has speed", sane.hasSpeed());
        Assert.assertSame("Still the same", reference, sane);

        sut.stopLogging();
    }

    @SmallTest
    public void testNormalAltitudeChange() {
        sut.onCreate();
        referenceLocation = sut.locationFilter(referenceLocation);
        referenceLocation.setLatitude(referenceLocation.getLatitude() + 0.0001d);
        referenceLocation = sut.locationFilter(referenceLocation);
        referenceLocation.setLatitude(referenceLocation.getLatitude() + 0.0001d);
        referenceLocation = sut.locationFilter(referenceLocation);

        Location reference = new Location(referenceLocation);
        reference.setLatitude(reference.getLatitude() + 0.0001d);
        reference.setTime(reference.getTime() + 6000l); // In 6 seconds times
        reference.setSpeed(4f);
        reference.setAccuracy(9f);
        reference.setAltitude(14.3d);
        sut.storeLocation(this.referenceLocation);

        Location sane = sut.locationFilter(reference);

        Assert.assertNotNull("Filter result", sane);
        Assert.assertTrue("Has altitude", referenceLocation.hasAltitude());
        Assert.assertTrue("Has altitude", sane.hasAltitude());
        Assert.assertSame("Still the same", reference, sane);

        sut.stopLogging();
    }

    @SmallTest
    public void testInsaneAltitudeChange() {
        when(mockPersistence.isSpeedChecked()).thenReturn(true);
        sut.onCreate();
        referenceLocation = sut.locationFilter(referenceLocation);
        referenceLocation.setLatitude(referenceLocation.getLatitude() + 0.0001d);
        referenceLocation = sut.locationFilter(referenceLocation);
        referenceLocation.setLatitude(referenceLocation.getLatitude() + 0.0001d);
        referenceLocation = sut.locationFilter(referenceLocation);

        Location reference = new Location(referenceLocation);
        reference.setLatitude(reference.getLatitude() + 0.0001d);
        reference.setSpeed(4f);
        reference.setAccuracy(9f);
        reference.setAltitude(514.3d);
        Location sane = sut.locationFilter(reference);

        Assert.assertTrue("Has altitude", referenceLocation.hasAltitude());
        Assert.assertFalse("Has no altitude", sane.hasAltitude());
        Assert.assertSame("Still the same", reference, sane);

        referenceLocation.setLatitude(referenceLocation.getLatitude() + 0.0001d);
        referenceLocation = sut.locationFilter(referenceLocation);
        Assert.assertTrue("Has altitude", referenceLocation.hasAltitude());

        sut.stopLogging();
    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private class MyMockContentProvider extends MockContentProvider {
        MyMockContentProvider() {
            super(getInstrumentation().getContext());
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            String result = null;
            Uri track = ContentConstants.Tracks.CONTENT_URI;
            Uri segment = Uri.withAppendedPath(track, "1/segments");
            Uri waypoint = Uri.withAppendedPath(segment, "1/waypoints");

            if (track.equals(uri)) {
                result = "content://nl.sogeti.android.gpstracker/tracks/1";
            } else if (segment.equals(uri)) {
                result = "content://nl.sogeti.android.gpstracker/tracks/1/segments/1";
            } else if (waypoint.equals(uri)) {
                result = "content://nl.sogeti.android.gpstracker/tracks/1/segments/1/waypoints/1";
            } else {
                Timber.w("No mock insert for " + uri);
            }

            return Uri.parse(result);
        }
    }
}
