/*------------------------------------------------------------------------------
 **     Ident: Sogeti Smart Mobile Solutions
 **    Author: rene
 ** Copyright: (c) Apr 24, 2011 Sogeti Nederland B.V. All Rights Reserved.
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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import nl.sogeti.android.gpstracker.service.integration.ContentConstants;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Media;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.MetaData;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Segments;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Tracks;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Waypoints;
import timber.log.Timber;

/**
 * Goal of this Content Provider is to make the GPS Tracking information uniformly
 * available to this application and even other applications. The GPS-tracking
 * database can hold, tracks, segments or waypoints
 * <p/>
 * A track is an actual route taken from start to finish. All the GPS locations
 * collected are waypoints. Waypoints taken in sequence without loss of GPS-signal
 * are considered connected and are grouped in segments. A route is build up out of
 * 1 or more segments.
 * <p/>
 * For example:<br>
 * <code>content://nl.sogeti.android.gpstracker/tracks</code>
 * is the URI that returns all the stored tracks or starts a new track on insert
 * <p/>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2</code>
 * is the URI string that would return a single result row, the track with ID = 23.
 * <p/>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments</code> is the URI that returns
 * all the stored segments of a track with ID = 2 or starts a new segment on insert
 * <p/>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/waypoints</code> is the URI that returns
 * all the stored waypoints of a track with ID = 2
 * <p/>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments</code> is the URI that returns
 * all the stored segments of a track with ID = 2
 * <p/>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments/3</code> is
 * the URI string that would return a single result row, the segment with ID = 3 of a track with ID = 2 .
 * <p/>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments/1/waypoints</code> is the URI that
 * returns all the waypoints of a segment 1 of track 2.
 * <p/>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments/1/waypoints/52</code> is the URI string that
 * would return a single result row, the waypoint with ID = 52
 * <p/>
 * Media is stored under a waypoint and may be queried as:<br>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments/3/waypoints/22/media</code>
 * <p/>
 * <p/>
 * <p/>
 * All media for a segment can be queried with:<br>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments/3/media</code>
 * <p/>
 * All media for a track can be queried with:<br>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/media</code>
 * <p/>
 * <p/>
 * The whole set of collected media may be queried as:<br>
 * <code>content://nl.sogeti.android.gpstracker/media</code>
 * <p/>
 * A single media is stored with an ID, for instance ID = 12:<br>
 * <code>content://nl.sogeti.android.gpstracker/media/12</code>
 * <p/>
 * The whole set of collected media may be queried as:<br>
 * <code>content://nl.sogeti.android.gpstracker/media</code>
 * <p/>
 * <p/>
 * <p/>
 * Meta-data regarding a single waypoint may be queried as:<br>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments/3/waypoints/22/metadata</code>
 * <p/>
 * Meta-data regarding a single segment as whole may be queried as:<br>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/segments/3/metadata</code>
 * Note: This does not include meta-data of waypoints.
 * <p/>
 * Meta-data regarding a single track as a whole may be queried as:<br>
 * <code>content://nl.sogeti.android.gpstracker/tracks/2/metadata</code>
 * Note: This does not include meta-data of waypoints or segments.
 *
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 * @version $Id$
 */
public class GPStrackingProvider extends ContentProvider {
    /* Action types as numbers for using the UriMatcher */
    private static final int TRACKS = 1;
    private static final int SEGMENTS_ALL = 20;
    private static final int WAYPOINTS_ALL = 21;
    private static final int TRACK_ID = 2;
    private static final int TRACK_MEDIA = 3;
    private static final int TRACK_WAYPOINTS = 4;
    private static final int SEGMENTS = 5;
    private static final int SEGMENT_ID = 6;
    private static final int SEGMENT_MEDIA = 7;
    private static final int WAYPOINTS = 8;
    private static final int WAYPOINT_ID = 9;
    private static final int WAYPOINT_MEDIA = 10;
    private static final int SEARCH_SUGGEST_ID = 11;
    private static final int MEDIA = 13;
    private static final int MEDIA_ID = 14;
    private static final int TRACK_METADATA = 15;
    private static final int SEGMENT_METADATA = 16;
    private static final int WAYPOINT_METADATA = 17;
    private static final int METADATA = 18;
    private static final int METADATA_ID = 19;
    private static final String[] SUGGEST_PROJECTION =
            new String[]
                    {
                            Tracks._ID,
                            Tracks.NAME + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1,
                            "datetime(" + Tracks.CREATION_TIME + "/1000, 'unixepoch') as " + SearchManager
                                    .SUGGEST_COLUMN_TEXT_2,
                            Tracks._ID + " AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID

                    };

    private static final String LOCATION_TAG = "OpenGpsTracker";

    private static UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /*
     * Although it is documented that in addURI(null, path, 0) "path" should be an absolute path this does not seem to
     * work. A relative path gets the jobs done and matches an absolute path.
     */
    static {
        GPStrackingProvider.sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks", GPStrackingProvider.TRACKS);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#", GPStrackingProvider.TRACK_ID);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/media", GPStrackingProvider.TRACK_MEDIA);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/metadata", GPStrackingProvider
                .TRACK_METADATA);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/waypoints", GPStrackingProvider
                .TRACK_WAYPOINTS);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/segments", GPStrackingProvider.SEGMENTS);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/segments/#", GPStrackingProvider
                .SEGMENT_ID);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/segments/#/media", GPStrackingProvider
                .SEGMENT_MEDIA);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/segments/#/metadata",
                GPStrackingProvider.SEGMENT_METADATA);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/segments/#/waypoints",
                GPStrackingProvider.WAYPOINTS);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/segments/#/waypoints/#",
                GPStrackingProvider.WAYPOINT_ID);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/segments/#/waypoints/#/media",
                GPStrackingProvider.WAYPOINT_MEDIA);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "tracks/#/segments/#/waypoints/#/metadata",
                GPStrackingProvider.WAYPOINT_METADATA);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "segments", GPStrackingProvider.SEGMENTS_ALL);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "waypoints", GPStrackingProvider.WAYPOINTS_ALL);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "media", GPStrackingProvider.MEDIA);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "media/#", GPStrackingProvider.MEDIA_ID);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "metadata", GPStrackingProvider.METADATA);
        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "metadata/#", GPStrackingProvider.METADATA_ID);

        GPStrackingProvider.sURIMatcher.addURI(ContentConstants.GPS_TRACKS_AUTHORITY, "search_suggest_query", GPStrackingProvider
                .SEARCH_SUGGEST_ID);

    }

    private DatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        if (this.mDbHelper == null) {
            this.mDbHelper = new DatabaseHelper(getContext());
        }

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = GPStrackingProvider.sURIMatcher.match(uri);

        String tableName;
        String innerSelection = "1";
        String[] innerSelectionArgs = new String[]{};
        List<String> pathSegments = uri.getPathSegments();
        switch (match) {
            case TRACKS:
                tableName = Tracks.TRACKS;
                break;
            case TRACK_ID:
                tableName = Tracks.TRACKS;
                innerSelection = Tracks._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case SEGMENTS:
                tableName = Segments.SEGMENTS;
                innerSelection = Segments.TRACK + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case SEGMENT_ID:
                tableName = Segments.SEGMENTS;
                innerSelection = Segments.TRACK + " = ?  and " + Segments._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1), pathSegments.get(3)};
                break;
            case WAYPOINTS:
                tableName = Waypoints.WAYPOINTS;
                innerSelection = Waypoints.SEGMENT + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(3)};
                break;
            case WAYPOINT_ID:
                tableName = Waypoints.WAYPOINTS;
                innerSelection = Waypoints.SEGMENT + " =  ?  and " + Waypoints._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(3), pathSegments.get(5)};
                break;
            case TRACK_WAYPOINTS:
                tableName = Waypoints.WAYPOINTS + " INNER JOIN " + Segments.SEGMENTS + " ON " + Segments.SEGMENTS + "." + Segments
                        ._ID + "==" + Waypoints.SEGMENT;
                innerSelection = Segments.TRACK + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case GPStrackingProvider.MEDIA:
                tableName = Media.MEDIA;
                break;
            case GPStrackingProvider.MEDIA_ID:
                tableName = Media.MEDIA;
                innerSelection = Media._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case TRACK_MEDIA:
                tableName = Media.MEDIA;
                innerSelection = Media.TRACK + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case SEGMENT_MEDIA:
                tableName = Media.MEDIA;
                innerSelection = Media.TRACK + " = ? and " + Media.SEGMENT + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1), pathSegments.get(3)};
                break;
            case WAYPOINT_MEDIA:
                tableName = Media.MEDIA;
                innerSelection = Media.TRACK + " = ?  and " + Media.SEGMENT + " = ? and " + Media.WAYPOINT + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1), pathSegments.get(3), pathSegments.get(5)};
                break;
            case TRACK_METADATA:
                tableName = MetaData.METADATA;
                innerSelection = MetaData.TRACK + " = ? and " + MetaData.SEGMENT + " = ? and " + MetaData.WAYPOINT + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1), "-1", "-1"};
                break;
            case SEGMENT_METADATA:
                tableName = MetaData.METADATA;
                innerSelection = MetaData.TRACK + " = ? and " + MetaData.SEGMENT + " = ? and " + MetaData.WAYPOINT + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1), pathSegments.get(3), "-1"};
                break;
            case WAYPOINT_METADATA:
                tableName = MetaData.METADATA;
                innerSelection = MetaData.TRACK + " = ? and " + MetaData.SEGMENT + " = ? and " + MetaData.WAYPOINT + " = ? " +
                        "? ";
                innerSelectionArgs = new String[]{pathSegments.get(1), pathSegments.get(3), pathSegments.get(5)};
                break;
            case GPStrackingProvider.METADATA:
                tableName = MetaData.METADATA;
                break;
            case GPStrackingProvider.METADATA_ID:
                tableName = MetaData.METADATA;
                innerSelection = MetaData._ID + " = ? ";
                innerSelectionArgs = new String[]{pathSegments.get(1)};
                break;
            case SEARCH_SUGGEST_ID:
                tableName = Tracks.TRACKS;
                if (selectionArgs[0] == null || selectionArgs[0].equals("")) {
                    selection = null;
                    selectionArgs = null;
                    sortOrder = Tracks.CREATION_TIME + " desc";
                } else {
                    selectionArgs[0] = "%" + selectionArgs[0] + "%";
                }
                projection = SUGGEST_PROJECTION;
                break;
            case SEGMENTS_ALL:
                tableName = Segments.SEGMENTS;
                break;
            case WAYPOINTS_ALL:
                tableName = Waypoints.WAYPOINTS;
                break;
            default:
                Timber.e("Unable to come to an action in the query uri: " + uri.toString());
                return null;
        }

        return getQueryCursor(uri, projection, selection, selectionArgs, sortOrder, tableName, innerSelection, innerSelectionArgs);
    }

    @NonNull
    private Cursor getQueryCursor(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, String tableName, String innerSelection, String[] innerSelectionArgs) {
        // SQLiteQueryBuilder is a helper class that creates the
        // proper SQL syntax for us.
        SQLiteQueryBuilder qBuilder = new SQLiteQueryBuilder();
        qBuilder.setStrict(true);

        // Set the table we're querying.
        qBuilder.setTables(tableName);

        if (selection == null) {
            selection = innerSelection;
        } else {
            selection = "( " + innerSelection + " ) and " + selection;
        }
        LinkedList<String> allArgs = new LinkedList<>();
        if (selectionArgs == null) {
            allArgs.addAll(Arrays.asList(innerSelectionArgs));
        } else {
            allArgs.addAll(Arrays.asList(innerSelectionArgs));
            allArgs.addAll(Arrays.asList(selectionArgs));
        }
        selectionArgs = allArgs.toArray(innerSelectionArgs);

        // Make the query.
        SQLiteDatabase mDb = this.mDbHelper.getWritableDatabase();
        Cursor c = qBuilder.query(mDb, projection, selection, selectionArgs, null, null, sortOrder);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        int match = GPStrackingProvider.sURIMatcher.match(uri);
        String mime = null;
        switch (match) {
            case TRACKS:
                mime = Tracks.CONTENT_TYPE;
                break;
            case TRACK_ID:
                mime = Tracks.CONTENT_ITEM_TYPE;
                break;
            case SEGMENTS:
                mime = Segments.CONTENT_TYPE;
                break;
            case SEGMENTS_ALL:
                mime = Segments.CONTENT_TYPE;
                break;
            case SEGMENT_ID:
                mime = Segments.CONTENT_ITEM_TYPE;
                break;
            case WAYPOINTS:
                mime = Waypoints.CONTENT_TYPE;
                break;
            case WAYPOINTS_ALL:
                mime = Waypoints.CONTENT_TYPE;
                break;
            case WAYPOINT_ID:
                mime = Waypoints.CONTENT_ITEM_TYPE;
                break;
            case MEDIA_ID:
            case TRACK_MEDIA:
            case SEGMENT_MEDIA:
            case WAYPOINT_MEDIA:
                mime = Media.CONTENT_ITEM_TYPE;
                break;
            case METADATA_ID:
            case TRACK_METADATA:
            case SEGMENT_METADATA:
            case WAYPOINT_METADATA:
                mime = MetaData.CONTENT_ITEM_TYPE;
                break;
            case UriMatcher.NO_MATCH:
            default:
                Timber.w("There is not MIME type defined for URI " + uri);
                break;
        }

        return mime;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Uri insertedUri;
        int match = GPStrackingProvider.sURIMatcher.match(uri);
        List<String> pathSegments;
        long trackId;
        long segmentId;
        long waypointId;
        long mediaId;
        String key;
        String value;
        switch (match) {
            case WAYPOINTS:
                pathSegments = uri.getPathSegments();
                trackId = Long.parseLong(pathSegments.get(1));
                segmentId = Long.parseLong(pathSegments.get(3));
                Location loc = new Location(LOCATION_TAG);
                Double latitude = values.getAsDouble(Waypoints.LATITUDE);
                Double longitude = values.getAsDouble(Waypoints.LONGITUDE);
                Long time = values.getAsLong(Waypoints.TIME);
                Float speed = values.getAsFloat(Waypoints.SPEED);
                if (time == null) {
                    time = System.currentTimeMillis();
                }
                if (speed == null) {
                    speed = 0f;
                }
                loc.setLatitude(latitude);
                loc.setLongitude(longitude);
                loc.setTime(time);
                loc.setSpeed(speed);

                if (values.containsKey(Waypoints.ACCURACY)) {
                    loc.setAccuracy(values.getAsFloat(Waypoints.ACCURACY));
                }
                if (values.containsKey(Waypoints.ALTITUDE)) {
                    loc.setAltitude(values.getAsDouble(Waypoints.ALTITUDE));

                }
                if (values.containsKey(Waypoints.BEARING)) {
                    loc.setBearing(values.getAsFloat(Waypoints.BEARING));
                }
                waypointId = this.mDbHelper.insertWaypoint(
                        trackId,
                        segmentId,
                        loc);
                insertedUri = ContentUris.withAppendedId(uri, waypointId);
                break;
            case WAYPOINT_MEDIA:
                pathSegments = uri.getPathSegments();
                trackId = Long.parseLong(pathSegments.get(1));
                segmentId = Long.parseLong(pathSegments.get(3));
                waypointId = Long.parseLong(pathSegments.get(5));
                String mediaUri = values.getAsString(Media.URI);
                mediaId = this.mDbHelper.insertMedia(trackId, segmentId, waypointId, mediaUri);
                insertedUri = ContentUris.withAppendedId(Media.MEDIA_URI, mediaId);
                break;
            case SEGMENTS:
                pathSegments = uri.getPathSegments();
                trackId = Integer.parseInt(pathSegments.get(1));
                segmentId = this.mDbHelper.toNextSegment(trackId);
                insertedUri = ContentUris.withAppendedId(uri, segmentId);
                break;
            case TRACKS:
                String name = (values == null) ? "" : values.getAsString(Tracks.NAME);
                trackId = this.mDbHelper.toNextTrack(name);
                insertedUri = ContentUris.withAppendedId(uri, trackId);
                break;
            case TRACK_METADATA:
                pathSegments = uri.getPathSegments();
                trackId = Long.parseLong(pathSegments.get(1));
                key = values.getAsString(MetaData.KEY);
                value = values.getAsString(MetaData.VALUE);
                mediaId = this.mDbHelper.insertOrUpdateMetaData(trackId, -1L, -1L, key, value);
                insertedUri = ContentUris.withAppendedId(MetaData.METADATA_URI, mediaId);
                break;
            case SEGMENT_METADATA:
                pathSegments = uri.getPathSegments();
                trackId = Long.parseLong(pathSegments.get(1));
                segmentId = Long.parseLong(pathSegments.get(3));
                key = values.getAsString(MetaData.KEY);
                value = values.getAsString(MetaData.VALUE);
                mediaId = this.mDbHelper.insertOrUpdateMetaData(trackId, segmentId, -1L, key, value);
                insertedUri = ContentUris.withAppendedId(MetaData.METADATA_URI, mediaId);
                break;
            case WAYPOINT_METADATA:
                pathSegments = uri.getPathSegments();
                trackId = Long.parseLong(pathSegments.get(1));
                segmentId = Long.parseLong(pathSegments.get(3));
                waypointId = Long.parseLong(pathSegments.get(5));
                key = values.getAsString(MetaData.KEY);
                value = values.getAsString(MetaData.VALUE);
                mediaId = this.mDbHelper.insertOrUpdateMetaData(trackId, segmentId, waypointId, key, value);
                insertedUri = ContentUris.withAppendedId(MetaData.METADATA_URI, mediaId);
                break;
            default:
                Timber.e("Unable to match the insert URI: " + uri.toString());
                insertedUri = null;
                break;
        }

        return insertedUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] valuesArray) {
        int inserted;
        int match = GPStrackingProvider.sURIMatcher.match(uri);
        switch (match) {
            case WAYPOINTS:
                List<String> pathSegments = uri.getPathSegments();
                int trackId = Integer.parseInt(pathSegments.get(1));
                int segmentId = Integer.parseInt(pathSegments.get(3));
                inserted = this.mDbHelper.bulkInsertWaypoint(trackId, segmentId, valuesArray);
                break;
            default:
                inserted = super.bulkInsert(uri, valuesArray);
                break;
        }

        return inserted;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int match = GPStrackingProvider.sURIMatcher.match(uri);
        int affected;
        switch (match) {
            case GPStrackingProvider.TRACK_ID:
                affected = this.mDbHelper.deleteTrack(Long.valueOf(uri.getLastPathSegment()));
                break;
            case GPStrackingProvider.MEDIA_ID:
                affected = this.mDbHelper.deleteMedia(Long.valueOf(uri.getLastPathSegment()));
                break;
            case GPStrackingProvider.METADATA_ID:
                affected = this.mDbHelper.deleteMetaData(Long.valueOf(uri.getLastPathSegment()));
                break;
            default:
                affected = 0;
                break;
        }

        return affected;
    }

    @Override
    public int update(Uri uri, ContentValues givenValues, String selection, String[] selectionArgs) {
        int updates;
        long trackId;
        long segmentId;
        long waypointId;
        long metaDataId;
        List<String> pathSegments;

        int match = GPStrackingProvider.sURIMatcher.match(uri);
        String value;
        switch (match) {
            case TRACK_ID:
                trackId = Long.parseLong(uri.getLastPathSegment());
                String name = givenValues.getAsString(Tracks.NAME);
                updates = mDbHelper.updateTrack(trackId, name);
                break;
            case TRACK_METADATA:
                pathSegments = uri.getPathSegments();
                trackId = Long.parseLong(pathSegments.get(1));
                value = givenValues.getAsString(MetaData.VALUE);
                updates = mDbHelper.updateMetaData(trackId, -1L, -1L, -1L, selection, selectionArgs, value);
                break;
            case SEGMENT_METADATA:
                pathSegments = uri.getPathSegments();
                trackId = Long.parseLong(pathSegments.get(1));
                segmentId = Long.parseLong(pathSegments.get(3));
                value = givenValues.getAsString(MetaData.VALUE);
                updates = mDbHelper.updateMetaData(trackId, segmentId, -1L, -1L, selection, selectionArgs, value);
                break;
            case WAYPOINT_METADATA:
                pathSegments = uri.getPathSegments();
                trackId = Long.parseLong(pathSegments.get(1));
                segmentId = Long.parseLong(pathSegments.get(3));
                waypointId = Long.parseLong(pathSegments.get(5));
                value = givenValues.getAsString(MetaData.VALUE);
                updates = mDbHelper.updateMetaData(trackId, segmentId, waypointId, -1L, selection, selectionArgs, value);
                break;
            case METADATA_ID:
                pathSegments = uri.getPathSegments();
                metaDataId = Long.parseLong(pathSegments.get(1));
                value = givenValues.getAsString(MetaData.VALUE);
                updates = mDbHelper.updateMetaData(-1L, -1L, -1L, metaDataId, selection, selectionArgs, value);
                break;
            default:
                Timber.e("Unable to come to an action in the query uri" + uri.toString());
                return -1;
        }

        return updates;
    }
}
