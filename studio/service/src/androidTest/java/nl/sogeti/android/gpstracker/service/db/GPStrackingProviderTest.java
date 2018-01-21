/*------------------------------------------------------------------------------
 **     Ident: Innovation en Inspiration > Google Android 
 **    Author: rene
 ** Copyright: (c) Jan 22, 2009 Sogeti Nederland B.V. All Rights Reserved.
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
package nl.sogeti.android.gpstracker.service.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import nl.sogeti.android.gpstracker.integration.ContentConstants;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Media;
import nl.sogeti.android.gpstracker.integration.ContentConstants.MetaData;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Segments;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints;

/**
 * Basically test that the functions offered by the content://nl.sogeti.android.gpstracker does what is documented.
 *
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 * @version $Id$
 */
public class GPStrackingProviderTest extends ProviderTestCase2<GPStrackingProvider> {

    private ContentResolver mResolver;

    public GPStrackingProviderTest() {
        super(GPStrackingProvider.class, ContentConstants.AUTHORITY);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.mResolver = getMockContentResolver();
    }

    @SmallTest
    public void testQuerySegmentsCursor() {
        Cursor cursor = this.mResolver.query(Uri.withAppendedPath(Tracks.CONTENT_URI, "1/segments"), null, null, null,
                null);
        Assert.assertNotNull("Curson should not be null", cursor);
        Assert.assertTrue("Curson should be a cursor", cursor instanceof android.database.Cursor);
        Assert.assertEquals("No segments are loaded", 0, cursor.getCount());
        cursor.close();
    }

    @SmallTest
    public void testQueryTracksCursor() {
        Cursor cursor = this.mResolver.query(Tracks.CONTENT_URI, null, null, null, null);
        Assert.assertNotNull("Curson should not be null", cursor);
        Assert.assertTrue("Curson should be a cursor", cursor instanceof android.database.Cursor);
        Assert.assertEquals("No tracks are loaded", 0, cursor.getCount());
        cursor.close();
    }

    @SmallTest
    public void testQueryWaypointsCursor() {
        Cursor cursor = this.mResolver.query(Uri.withAppendedPath(Tracks.CONTENT_URI, "1/segments/1/waypoints"), null,
                null, null, null);
        Assert.assertNotNull("Curson should not be null", cursor);
        Assert.assertTrue("Curson should be a cursor", cursor instanceof android.database.Cursor);
        Assert.assertEquals("No waypoints are loaded", 0, cursor.getCount());
        cursor.close();
    }

    @SmallTest
    public void testStartTracks() {
        Uri firstTrack = Uri.parse(Tracks.CONTENT_URI + "/1");
        Uri secondTrack = Uri.parse(Tracks.CONTENT_URI + "/2");
        Uri newTrackUri;

        newTrackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Assert.assertEquals("Fresh new track 1", firstTrack, newTrackUri);

        newTrackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Assert.assertEquals("Fresh new track 2", secondTrack, newTrackUri);
    }

    /**
     * Create a track with a name
     */
    @SmallTest
    public void testStartTracksWithName() {
        String testname = "testStartTracksWithName";

        ContentValues values = new ContentValues();
        values.put(Tracks.NAME, testname);
        Uri newTrackUri = this.mResolver.insert(Tracks.CONTENT_URI, values);

        Cursor trackCursor = this.mResolver.query(newTrackUri, new String[]{Tracks.NAME}, null, null, null);
        Assert.assertTrue("Should be possble to move to the first track", trackCursor.moveToFirst());
        Assert.assertEquals("This track query should have 1 track", 1, trackCursor.getCount());
        Assert.assertEquals("Name should be the same", testname, trackCursor.getString(0));
        trackCursor.close();
    }

    /**
     * Create a track with a name
     */
    @SmallTest
    public void testUpdateTrackWithName() {
        Cursor trackCursor;
        Uri newTrackUri;
        String testname = "testUpdateTrackWithName";

        newTrackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        trackCursor = this.mResolver.query(newTrackUri, new String[]{Tracks.NAME}, null, null, null);
        Assert.assertTrue("Should be possble to move to the first track", trackCursor.moveToFirst());
        Assert.assertEquals("This track query should have 1 track", 1, trackCursor.getCount());
        Assert.assertEquals("Name should be the same", "", trackCursor.getString(0));

        ContentValues values = new ContentValues();
        values.put(Tracks.NAME, testname);
        int updates = this.mResolver.update(newTrackUri, values, null, null);
        trackCursor.requery();
        Assert.assertEquals("One row should be updated", 1, updates);
        Assert.assertTrue("Should be possble to move to the first track", trackCursor.moveToFirst());
        Assert.assertEquals("This track query should have 1 track", 1, trackCursor.getCount());
        Assert.assertEquals("Name should be the same", testname, trackCursor.getString(0));
        trackCursor.close();
    }

    @SmallTest
    public void testTrackAltitudeWaypoint() {
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(200d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(100d));
        wp.put(Waypoints.ALTITUDE, Double.valueOf(-123.456d));

        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypoint = this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);

        Cursor waypointCursor = this.mResolver.query(waypoint, new String[]{Waypoints.LONGITUDE, Waypoints.LATITUDE,
                Waypoints.ALTITUDE}, null, null, null);
        Assert.assertEquals("This segment should list waypoints", 1, waypointCursor.getCount());
        Assert.assertTrue("Should be possble to move to the first waypoint", waypointCursor.moveToFirst());
        Assert.assertEquals("Longitude", 200d, waypointCursor.getDouble(0));
        Assert.assertEquals("Latitude", 100d, waypointCursor.getDouble(1));
        Assert.assertEquals("Altitude", -123.456d, waypointCursor.getDouble(2));
        waypointCursor.close();
    }

    @SmallTest
    public void testTrackBearingWaypoint() {
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(200d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(100d));
        wp.put(Waypoints.BEARING, Float.valueOf(23.456f));

        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypoint = this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);

        Cursor waypointCursor = this.mResolver.query(waypoint, new String[]{Waypoints.LONGITUDE, Waypoints.LATITUDE,
                Waypoints.BEARING}, null, null, null);
        Assert.assertEquals("This segment should list waypoints", 1, waypointCursor.getCount());
        Assert.assertTrue("Should be possble to move to the first waypoint", waypointCursor.moveToFirst());
        Assert.assertEquals("Longitude", 200d, waypointCursor.getDouble(0));
        Assert.assertEquals("Latitude", 100d, waypointCursor.getDouble(1));
        Assert.assertEquals("Bearing", 23.456f, waypointCursor.getFloat(2));
        waypointCursor.close();
    }

    @SmallTest
    public void testTrackAccuracyWaypoint() {
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(200d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(100d));
        wp.put(Waypoints.ACCURACY, Float.valueOf(-123.456f));

        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypoint = this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);

        Cursor waypointCursor = this.mResolver.query(waypoint, new String[]{Waypoints.LONGITUDE, Waypoints.LATITUDE,
                Waypoints.ACCURACY}, null, null, null);
        Assert.assertEquals("This segment should list waypoints", 1, waypointCursor.getCount());
        Assert.assertTrue("Should be possble to move to the first waypoint", waypointCursor.moveToFirst());
        Assert.assertEquals("Longitude", 200d, waypointCursor.getDouble(0));
        Assert.assertEquals("Latitude", 100d, waypointCursor.getDouble(1));
        Assert.assertEquals("Accuracy", -123.456f, waypointCursor.getFloat(2));
        waypointCursor.close();
    }

    /**
     * Start a track, 1 segment ,insert 2 waypoints and expect 1 track with 1 segment with the 2 waypoints that where
     * inserted
     */
    @SmallTest
    public void testTrackWaypointWaypoint() {
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(200d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(100d));

        // E.g. returns: content://nl.sogeti.android.gpstracker/tracks/2
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);

        this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);
        this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);

        // E.g. content://nl.sogeti.android.gpstracker/tracks/2/segments
        Uri segments = Uri.withAppendedPath(trackUri, "segments");
        Cursor trackCursor = this.mResolver.query(segments, new String[]{Segments._ID}, null, null, null);
        trackCursor.moveToFirst();
        int segmentId = trackCursor.getInt(0);
        Assert.assertEquals("This track should have a segment", 1, trackCursor.getCount());
        Assert.assertTrue("Should be possble to move to the first track", trackCursor.moveToFirst());
        trackCursor.close();

        // E.g. content://nl.sogeti.android.gpstracker/segments/1/waypoints
        Uri waypoints = Uri.withAppendedPath(Tracks.CONTENT_URI, "1/segments/" + segmentId + "/waypoints");
        Cursor waypointCursor = this.mResolver.query(waypoints, new String[]{Waypoints.LONGITUDE, Waypoints.LATITUDE
        }, null, null, null);
        Assert.assertEquals("This segment should list waypoints", 2, waypointCursor.getCount());
        Assert.assertTrue("Should be possble to move to the first waypoint", waypointCursor.moveToFirst());

        do {
            Assert.assertEquals("First Longitude", 200d, waypointCursor.getDouble(0));
            Assert.assertEquals("First Latitude", 100d, waypointCursor.getDouble(1));
        }
        while (waypointCursor.moveToNext());
        waypointCursor.close();
    }

    /**
     * Create 2 tracks and a segments in each and two waypoints in each segment
     */
    @SmallTest
    public void testMakeTwoTracks() {
        String testname = "track";
        Uri trackOneUri;
        ContentValues values;
        Cursor trackCursor;
        double coord = 1d;
        ContentValues wp;

        values = new ContentValues();
        values.put(Tracks.NAME, testname + 1);
        trackOneUri = this.mResolver.insert(Tracks.CONTENT_URI, values);

        Uri segmentOneUri = this.mResolver.insert(Uri.withAppendedPath(trackOneUri, "segments"), values);
        Uri waypointsOneUri = Uri.withAppendedPath(segmentOneUri, "waypoints");

        Cursor waypointsOneCursor = this.mResolver.query(waypointsOneUri, new String[]{}, null, null, null);
        Assert.assertEquals("We should now have 0 waypoints", 0, waypointsOneCursor.getCount());
        waypointsOneCursor.close();

        wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(coord));
        wp.put(Waypoints.LATITUDE, Double.valueOf(coord));
        this.mResolver.insert(waypointsOneUri, wp);
        coord++;

        wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(coord));
        wp.put(Waypoints.LATITUDE, Double.valueOf(coord));
        this.mResolver.insert(waypointsOneUri, wp);
        coord++;

        trackCursor = this.mResolver.query(trackOneUri, new String[]{Tracks.NAME}, null, null, null);
        Assert.assertTrue("Should be possble to move to the first track", trackCursor.moveToFirst());
        Assert.assertEquals("This track query should have 1 track", 1, trackCursor.getCount());
        Assert.assertEquals("Name should be the same", testname + 1, trackCursor.getString(0));
        trackCursor.close();

        values = new ContentValues();
        values.put(Tracks.NAME, testname + 2);
        Uri trackTwoUri = this.mResolver.insert(Tracks.CONTENT_URI, values);
        Cursor trackTwoCursor = this.mResolver.query(trackTwoUri, new String[]{Tracks.NAME}, null, null, null);
        Assert.assertTrue("Should be possble to move to the first track", trackTwoCursor.moveToFirst());
        Assert.assertEquals("This track query should have 1 track", 1, trackTwoCursor.getCount());
        Assert.assertEquals("Name should be the same", testname + 2, trackTwoCursor.getString(0));
        trackTwoCursor.close();
        Uri segmentTwoUri = this.mResolver.insert(Uri.withAppendedPath(trackTwoUri, "segments"), values);
        Uri waypointsTwoUri = Uri.withAppendedPath(segmentTwoUri, "waypoints");

        wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(coord));
        wp.put(Waypoints.LATITUDE, Double.valueOf(coord));
        this.mResolver.insert(waypointsTwoUri, wp);
        coord++;
        wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(coord));
        wp.put(Waypoints.LATITUDE, Double.valueOf(coord));
        this.mResolver.insert(waypointsTwoUri, wp);
        coord++;

        Cursor waypointsTwoCursor = this.mResolver.query(waypointsTwoUri, new String[]{Waypoints.SEGMENT}, null,
                null, null);
        Assert.assertEquals("We should now have 2 waypoints", 2, waypointsTwoCursor.getCount());
        Assert.assertTrue("Working", waypointsTwoCursor.moveToFirst());
        waypointsTwoCursor.close();
    }

    @SmallTest
    public void testDeleteEmptyTrack() {
        // E.g. returns: content://nl.sogeti.android.gpstracker/tracks/2
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Cursor trackCursor = this.mResolver.query(trackUri, new String[]{Tracks._ID}, null, null, null);
        Assert.assertEquals("One track inserted", 1, trackCursor.getCount());

        int affected = this.mResolver.delete(trackUri, null, null);
        Assert.assertEquals("One track deleted", 1, affected);

        trackCursor.requery();

        Assert.assertEquals("No track left", 0, trackCursor.getCount());
        trackCursor.close();
    }

    @SmallTest
    public void testDeleteSimpleTrack() {
        ContentValues wp;
        double coord = 1d;
        // E.g. returns: content://nl.sogeti.android.gpstracker/tracks/2
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Cursor trackCursor = this.mResolver.query(trackUri, new String[]{Tracks._ID}, null, null, null);
        // E.g. returns: content://nl.sogeti.android.gpstracker/tracks/2/segments/1
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Cursor segmentCursor = this.mResolver.query(segmentUri, new String[]{Segments._ID}, null, null, null);
        Assert.assertEquals("One track created", 1, trackCursor.getCount());
        Assert.assertEquals("One segment created", 1, segmentCursor.getCount());
        // Stuff 2 waypoints as the segment contents
        wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(coord));
        wp.put(Waypoints.LATITUDE, Double.valueOf(coord));
        Uri wp1 = this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);
        wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(coord));
        wp.put(Waypoints.LATITUDE, Double.valueOf(coord));
        Uri wp2 = this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);

        // Pivot of the test case: THE DELETE
        int affected = this.mResolver.delete(trackUri, null, null);

        Assert.assertEquals("One track, one segments and two waypoints deleted", 4, affected);
        Assert.assertTrue("The cursor to the track is still valid", trackCursor.requery());
        Assert.assertEquals("No track left", 0, trackCursor.getCount());
        Assert.assertTrue("The cursor to the segments is still valid", segmentCursor.requery());
        Assert.assertEquals("No segments left", 0, segmentCursor.getCount());
        Cursor wpCursor = this.mResolver.query(wp1, null, null, null, null);
        Assert.assertEquals("Waypoint 1 is gone", 0, wpCursor.getCount());
        wpCursor.close();
        wpCursor = this.mResolver.query(wp2, null, null, null, null);
        Assert.assertEquals("Waypoint 2 is gone", 0, wpCursor.getCount());
        wpCursor.close();
        trackCursor.close();
        segmentCursor.close();
    }

    @SmallTest
    public void testDeleteSegmentedTrack() {
        ContentValues wp;
        double coord = 1d;
        // E.g. returns: content://nl.sogeti.android.gpstracker/tracks/2
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Cursor trackCursor = this.mResolver.query(trackUri, new String[]{Tracks._ID}, null, null, null);

        Cursor[] segmentCursor = new Cursor[2];
        for (int i = 0; i < 2; i++) {
            // E.g. returns: content://nl.sogeti.android.gpstracker/tracks/2/segments/1
            Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
            segmentCursor[i] = this.mResolver.query(segmentUri, new String[]{Segments._ID}, null, null, null);

            // Stuff 2 waypoints as the segment contents
            wp = new ContentValues();
            wp.put(Waypoints.LONGITUDE, Double.valueOf(coord));
            wp.put(Waypoints.LATITUDE, Double.valueOf(coord));
            this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);
            wp = new ContentValues();
            wp.put(Waypoints.LONGITUDE, Double.valueOf(coord));
            wp.put(Waypoints.LATITUDE, Double.valueOf(coord));
            this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);
        }

        // Pivot of the test case: THE DELETE
        int affected = this.mResolver.delete(trackUri, null, null);

        Assert.assertEquals("One track, two segments and four waypoints deleted", 7, affected);
        Assert.assertTrue("The cursor to the track is still valid", trackCursor.requery());
        Assert.assertEquals("No track left", 0, trackCursor.getCount());

        for (int i = 0; i < 2; i++) {
            Assert.assertTrue("The cursor to the segments is still valid", segmentCursor[i].requery());
            Assert.assertEquals("No segments left", 0, segmentCursor[i].getCount());
        }
        trackCursor.close();
        for (int i = 0; i < 2; i++) {
            segmentCursor[i].close();
        }
    }

    /**
     * Insert a waypoint with a time and expect that same time to return
     */
    @SmallTest
    public void testWaypointTime() {
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(200d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(100d));
        long msTime = 1234567890000l;
        wp.put(Waypoints.TIME, Long.valueOf(msTime));

        // E.g. returns: content://nl.sogeti.android.gpstracker/tracks/2
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypointUri = this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);

        Cursor waypointCursor = this.mResolver.query(waypointUri, new String[]{Waypoints.TIME}, null, null, null);
        waypointCursor.moveToFirst();
        Location location = new Location("testWaypointTime");
        location.setTime(waypointCursor.getLong(0));
        Assert.assertEquals("Time should remain unchanged", msTime, location.getTime());
        waypointCursor.close();
    }

    /**
     * Insert a waypoint with a time and expect that same time to return
     */
    @SmallTest
    public void testInsertHighPrecisionAndExportHighPrecision() {
        double lon = 5.123456789d;
        double lat = 51.123456789d;
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(lon));
        wp.put(Waypoints.LATITUDE, Double.valueOf(lat));
        wp.put(Waypoints.TIME, Long.valueOf(1234567890000l));

        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypointUri = this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);

        Cursor waypointCursor = this.mResolver.query(waypointUri, new String[]{Waypoints.LONGITUDE, Waypoints
                .LATITUDE}, null, null, null);
        waypointCursor.moveToFirst();
        Assert.assertEquals("Longitude", lon, waypointCursor.getDouble(0));
        Assert.assertEquals("Latitude", lat, waypointCursor.getDouble(1));
        Assert.assertEquals("Longitude string", "5.123456789", Double.toString(waypointCursor.getDouble(0)));
        Assert.assertEquals("Latitude string", "51.123456789", Double.toString(waypointCursor.getDouble(1)));

        waypointCursor.close();
    }

    /**
     * GPX export precision is too low, so it creates weird walking tracks in Google Earth http://code.google
     * .com/p/open-gpstracker/issues/detail?id=81
     */
    public void testInsertLargeNegativeAndExportHighPrecision() {
        double lon = 37.8657d;
        double lat = -122.305d;
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(lon));
        wp.put(Waypoints.LATITUDE, Double.valueOf(lat));
        wp.put(Waypoints.TIME, Long.valueOf(1234567890000l));

        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypointUri = this.mResolver.insert(Uri.withAppendedPath(segmentUri, "waypoints"), wp);

        Cursor waypointCursor = this.mResolver.query(waypointUri, new String[]{Waypoints.LONGITUDE, Waypoints
                .LATITUDE}, null, null, null);
        waypointCursor.moveToFirst();
        Assert.assertEquals("Longitude", lon, waypointCursor.getDouble(0));
        Assert.assertEquals("Latitude", lat, waypointCursor.getDouble(1));
        Assert.assertEquals("Longitude string", "37.8657", Double.toString(waypointCursor.getDouble(0)));
        Assert.assertEquals("Latitude string", "-122.305", Double.toString(waypointCursor.getDouble(1)));

        waypointCursor.close();
    }

    @SmallTest
    public void testInsertMedia() {
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypointsUri = Uri.withAppendedPath(segmentUri, "waypoints");
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(37.8657d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(-122.305d));
        Uri waypointUri = this.mResolver.insert(waypointsUri, wp);

        Uri trackMediaUri = Uri.withAppendedPath(trackUri, "media");
        Uri segmentMediaUri = Uri.withAppendedPath(segmentUri, "media");
        Uri waypointsMediaUri = Uri.withAppendedPath(waypointUri, "media");
        Cursor trackMedia = this.mResolver.query(trackMediaUri, new String[]{Media.URI}, null, null, null);
        Cursor segmentMedia = this.mResolver.query(segmentMediaUri, new String[]{Media.URI}, null, null, null);
        Cursor waypointMedia = this.mResolver.query(waypointsMediaUri, new String[]{Media.URI}, null, null, null);

        Assert.assertEquals("No track media", 0, trackMedia.getCount());
        Assert.assertEquals("No segment media", 0, segmentMedia.getCount());
        Assert.assertEquals("No waypoint media", 0, waypointMedia.getCount());

        Uri mediaInsertUri = Uri.withAppendedPath(waypointUri, "media");
        ContentValues args = new ContentValues();
        args.put(Media.URI, mediaInsertUri.toString());
        Uri mediaUri = this.mResolver.insert(mediaInsertUri, args);

        Assert.assertNotNull("Uri returned", mediaUri);
        Cursor media = this.mResolver.query(mediaUri, new String[]{Media.URI}, null, null, null);
        Assert.assertEquals("Insert successful", 1, media.getCount());
        trackMedia.requery();
        segmentMedia.requery();
        waypointMedia.requery();
        Assert.assertEquals("Single track media", 1, trackMedia.getCount());
        Assert.assertEquals("Single segment media", 1, segmentMedia.getCount());
        Assert.assertEquals("Single waypoint media", 1, waypointMedia.getCount());

        media.close();
        trackMedia.close();
        segmentMedia.close();
        waypointMedia.close();
    }

    @SmallTest
    public void testDeleteTrackWithMedia() {
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(37.8657d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(-122.305d));
        ContentValues args = new ContentValues();
        args.put(Media.URI, "a test");
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypointsUri = Uri.withAppendedPath(segmentUri, "waypoints");
        Uri waypointUri = this.mResolver.insert(waypointsUri, wp);

        Uri trackMediaUri = Uri.withAppendedPath(trackUri, "media");
        Uri segmentMediaUri = Uri.withAppendedPath(segmentUri, "media");
        Uri waypointsMediaUri = Uri.withAppendedPath(waypointUri, "media");
        Uri mediaInsertUri = Uri.withAppendedPath(waypointUri, "media");
        Uri mediaUri = this.mResolver.insert(mediaInsertUri, args);

        this.mResolver.delete(trackUri, null, null);

        Cursor trackMedia = this.mResolver.query(trackMediaUri, new String[]{Media.URI}, null, null, null);
        Cursor segmentMedia = this.mResolver.query(segmentMediaUri, new String[]{Media.URI}, null, null, null);
        Cursor waypointMedia = this.mResolver.query(waypointsMediaUri, new String[]{Media.URI}, null, null, null);
        Cursor media = this.mResolver.query(mediaUri, new String[]{Media.URI}, null, null, null);

        Assert.assertEquals("No track media", 0, trackMedia.getCount());
        Assert.assertEquals("No segment media", 0, segmentMedia.getCount());
        Assert.assertEquals("No waypoint media", 0, waypointMedia.getCount());
        Assert.assertEquals("No media", 0, media.getCount());

        trackMedia.close();
        segmentMedia.close();
        waypointMedia.close();
        media.close();
    }

    @SmallTest
    public void testInsertMetaData() {
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypointsUri = Uri.withAppendedPath(segmentUri, "waypoints");
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(37.8657d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(-122.305d));
        Uri waypointUri = this.mResolver.insert(waypointsUri, wp);

        Uri trackMetaDataUri = Uri.withAppendedPath(trackUri, "metadata");
        Uri segmentMetaDatari = Uri.withAppendedPath(segmentUri, "metadata");
        Uri waypointsMetaDataUri = Uri.withAppendedPath(waypointUri, "metadata");
        Cursor trackMetaData = this.mResolver.query(trackMetaDataUri, new String[]{MetaData.KEY, MetaData.VALUE},
                null, null, null);
        Cursor segmentMetaData = this.mResolver.query(segmentMetaDatari, new String[]{MetaData.KEY, MetaData.VALUE},
                null, null, null);
        Cursor waypointMetaData = this.mResolver.query(waypointsMetaDataUri, new String[]{MetaData.KEY, MetaData
                .VALUE}, null, null, null);

        Assert.assertEquals("No track metadata", 0, trackMetaData.getCount());
        Assert.assertEquals("No segment metadata", 0, segmentMetaData.getCount());
        Assert.assertEquals("No waypoint metadata", 0, waypointMetaData.getCount());

        Cursor metaData;
        Uri metaDataInsertUri;
        Uri mediaUri;
        ContentValues args = new ContentValues();
        args.put(MetaData.KEY, "A key");
        args.put(MetaData.VALUE, "A value");

        metaDataInsertUri = Uri.withAppendedPath(waypointUri, "metadata");
        mediaUri = this.mResolver.insert(metaDataInsertUri, args);
        Assert.assertNotNull("Uri returned", mediaUri);

        metaData = this.mResolver.query(mediaUri, new String[]{MetaData.KEY, MetaData.VALUE}, null, null, null);
        Assert.assertEquals("Insert successful", 1, metaData.getCount());
        trackMetaData.requery();
        segmentMetaData.requery();
        waypointMetaData.requery();
        Assert.assertEquals("Single waypoint media", 1, waypointMetaData.getCount());
        Assert.assertEquals("No segment media", 0, segmentMetaData.getCount());
        Assert.assertEquals("No track media", 0, trackMetaData.getCount());
        metaData.close();

        metaDataInsertUri = Uri.withAppendedPath(segmentUri, "metadata");
        mediaUri = this.mResolver.insert(metaDataInsertUri, args);
        Assert.assertNotNull("Uri returned", mediaUri);

        Assert.assertNotNull("Uri returned", mediaUri);
        metaData = this.mResolver.query(mediaUri, new String[]{MetaData.KEY, MetaData.VALUE}, null, null, null);
        Assert.assertEquals("Insert successful", 1, metaData.getCount());
        trackMetaData.requery();
        segmentMetaData.requery();
        waypointMetaData.requery();
        Assert.assertEquals("Single waypoint media", 1, waypointMetaData.getCount());
        Assert.assertEquals("Single segment media", 1, segmentMetaData.getCount());
        Assert.assertEquals("No track media", 0, trackMetaData.getCount());
        metaData.close();

        metaDataInsertUri = Uri.withAppendedPath(trackUri, "metadata");
        mediaUri = this.mResolver.insert(metaDataInsertUri, args);
        Assert.assertNotNull("Uri returned", mediaUri);

        Assert.assertNotNull("Uri returned", mediaUri);
        metaData = this.mResolver.query(mediaUri, new String[]{MetaData.KEY, MetaData.VALUE}, null, null, null);
        Assert.assertEquals("Insert successful", 1, metaData.getCount());
        trackMetaData.requery();
        segmentMetaData.requery();
        waypointMetaData.requery();
        Assert.assertEquals("Single waypoint media", 1, waypointMetaData.getCount());
        Assert.assertEquals("Single segment media", 1, segmentMetaData.getCount());
        Assert.assertEquals("Single track media", 1, trackMetaData.getCount());
        metaData.close();

        trackMetaData.close();
        segmentMetaData.close();
        waypointMetaData.close();
    }

    @SmallTest
    public void testDeleteTrackWithMetaData() {
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(37.8657d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(-122.305d));
        ContentValues args = new ContentValues();
        args.put(MetaData.KEY, "a test");
        args.put(MetaData.VALUE, "value");
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypointsUri = Uri.withAppendedPath(segmentUri, "waypoints");
        Uri waypointUri = this.mResolver.insert(waypointsUri, wp);

        Uri trackMetaDataUri = Uri.withAppendedPath(trackUri, "metadata");
        Uri segmentMetaDataUri = Uri.withAppendedPath(segmentUri, "metadata");
        Uri waypointsMetaDataUri = Uri.withAppendedPath(waypointUri, "metadata");

        this.mResolver.insert(trackMetaDataUri, args);
        this.mResolver.insert(segmentMetaDataUri, args);
        this.mResolver.insert(waypointsMetaDataUri, args);
        Cursor metadata = this.mResolver.query(MetaData.CONTENT_URI, new String[]{MetaData.KEY, MetaData.VALUE},
                null, null, null);
        Assert.assertTrue("Enough media", metadata.getCount() >= 3);

        this.mResolver.delete(trackUri, null, null);

        Cursor trackMetadata = this.mResolver.query(trackMetaDataUri, new String[]{MetaData.KEY, MetaData.VALUE},
                null, null, null);
        Cursor segmentMetadata = this.mResolver.query(segmentMetaDataUri, new String[]{MetaData.KEY, MetaData.VALUE
        }, null, null, null);
        Cursor waypointMetadata = this.mResolver.query(waypointsMetaDataUri, new String[]{MetaData.KEY, MetaData
                .VALUE}, null, null, null);
        metadata.requery();

        Assert.assertEquals("No track media", 0, trackMetadata.getCount());
        Assert.assertEquals("No segment media", 0, segmentMetadata.getCount());
        Assert.assertEquals("No waypoint media", 0, waypointMetadata.getCount());
        Assert.assertEquals("No media", 0, metadata.getCount());

        trackMetadata.close();
        segmentMetadata.close();
        waypointMetadata.close();
        metadata.close();
    }

    @SmallTest
    public void testUpdateTrackWithMetaData() {
        ContentValues wp = new ContentValues();
        wp.put(Waypoints.LONGITUDE, Double.valueOf(37.8657d));
        wp.put(Waypoints.LATITUDE, Double.valueOf(-122.305d));
        ContentValues args = new ContentValues();
        args.put(MetaData.KEY, "a test");
        args.put(MetaData.VALUE, "firstvalue");
        Uri trackUri = this.mResolver.insert(Tracks.CONTENT_URI, null);
        Uri segmentUri = this.mResolver.insert(Uri.withAppendedPath(trackUri, "segments"), null);
        Uri waypointsUri = Uri.withAppendedPath(segmentUri, "waypoints");
        Uri waypointUri = this.mResolver.insert(waypointsUri, wp);

        Uri trackMetaDataUri = Uri.withAppendedPath(trackUri, "metadata");
        Uri segmentMetaDataUri = Uri.withAppendedPath(segmentUri, "metadata");
        Uri waypointsMetaDataUri = Uri.withAppendedPath(waypointUri, "metadata");

        this.mResolver.insert(trackMetaDataUri, args);
        this.mResolver.insert(segmentMetaDataUri, args);
        this.mResolver.insert(waypointsMetaDataUri, args);
        Cursor metadata = this.mResolver.query(MetaData.CONTENT_URI, new String[]{MetaData.VALUE}, null, null, null);
        Assert.assertTrue("Enough media", metadata.getCount() >= 3);

        args = new ContentValues();
        args.put(MetaData.VALUE, "secondvalue");
        int trackRows = this.mResolver.update(trackMetaDataUri, args, MetaData.KEY + " = ? ", new String[]{"a test"});
        int segmentRows = this.mResolver.update(segmentMetaDataUri, args, MetaData.KEY + " = ? ", new String[]{"a " +
                "test"});
        int waypointRows = this.mResolver.update(waypointsMetaDataUri, args, MetaData.KEY + " = ? ", new String[]{"a " +
                "test"});

        Assert.assertEquals("Track meta-data updates", trackRows, 1);
        Assert.assertEquals("Segment meta-data updates", segmentRows, 1);
        Assert.assertEquals("Waypoint meta-data updates", waypointRows, 1);

        Cursor trackMetadata = this.mResolver.query(trackMetaDataUri, new String[]{MetaData.VALUE}, MetaData.KEY + "" +
                " = ? ", new String[]{"a test"}, null);
        Cursor segmentMetadata = this.mResolver.query(segmentMetaDataUri, new String[]{MetaData.VALUE}, MetaData.KEY
                + " = ? ", new String[]{"a test"}, null);
        Cursor waypointMetadata = this.mResolver.query(waypointsMetaDataUri, new String[]{MetaData.VALUE}, MetaData
                .KEY + " = ? ", new String[]{"a test"}, null);
        metadata.requery();

        Assert.assertEquals("A track metadata", 1, trackMetadata.getCount());
        Assert.assertEquals("A segment metadata", 1, segmentMetadata.getCount());
        Assert.assertEquals("A waypoint metadata", 1, waypointMetadata.getCount());
        Assert.assertTrue("Enough media", metadata.getCount() >= 3);

        Assert.assertTrue("track metadata", trackMetadata.moveToFirst());
        Assert.assertTrue("segment metadata", segmentMetadata.moveToFirst());
        Assert.assertTrue("waypoint metadata", waypointMetadata.moveToFirst());

        Assert.assertEquals("Tack meta", trackMetadata.getString(0), "secondvalue");
        Assert.assertEquals("segment meta", segmentMetadata.getString(0), "secondvalue");
        Assert.assertEquals("waypoint meta", waypointMetadata.getString(0), "secondvalue");

        trackMetadata.close();
        segmentMetadata.close();
        waypointMetadata.close();
        metadata.close();
    }
}
