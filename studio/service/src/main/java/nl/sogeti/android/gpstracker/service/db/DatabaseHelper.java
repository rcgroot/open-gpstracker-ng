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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.net.Uri;

import java.util.Date;

import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Media;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.MediaColumns;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.MetaData;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Segments;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Tracks;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.TracksColumns;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.Waypoints;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.WaypointsColumns;
import timber.log.Timber;

/**
 * Class to hold bare-metal database operations exposed as functionality blocks
 * To be used by database adapters, like a content provider, that implement a
 * required functionality set
 *
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 * @version $Id$
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DatabaseConstants.DATABASE_NAME, null, DatabaseConstants.DATABASE_VERSION);
        this.mContext = context;
    }

    /*
     * (non-Javadoc)
     * @see
     * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
     * .SQLiteDatabase)
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DatabaseConstants.Waypoints.CREATE_STATEMENT);
        db.execSQL(DatabaseConstants.Segments.CREATE_STATMENT);
        db.execSQL(DatabaseConstants.Tracks.CREATE_STATEMENT);
        db.execSQL(DatabaseConstants.Media.CREATE_STATEMENT);
        db.execSQL(DatabaseConstants.MetaData.CREATE_STATEMENT);
    }

    /**
     * Will update version 1 through 5 to version 8
     *
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase,
     * int, int)
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int current, int targetVersion) {
        Timber.i("Upgrading db from " + current + " to " + targetVersion);
        if (current <= 5) // From 1-5 to 6 (these before are the same before)
        {
            current = 6;
        }
        if (current == 6) // From 6 to 7 ( no changes )
        {
            current = 7;
        }
        if (current == 7) // From 7 to 8 ( more waypoints data )
        {
            for (String statement : DatabaseConstants.Waypoints.UPGRADE_STATEMENT_7_TO_8) {
                db.execSQL(statement);
            }
            current = 8;
        }
        if (current == 8) // From 8 to 9 ( media Uri data )
        {
            db.execSQL(DatabaseConstants.Media.CREATE_STATEMENT);
            current = 9;
        }
        if (current == 9) // From 9 to 10 ( metadata )
        {
            db.execSQL(DatabaseConstants.MetaData.CREATE_STATEMENT);
            current = 10;
        }
    }

    public void vacuum() {
        new Thread() {
            @Override
            public void run() {
                SQLiteDatabase sqldb = getWritableDatabase();
                sqldb.execSQL("VACUUM");
            }
        }.start();

    }

    int bulkInsertWaypoint(long trackId, long segmentId, ContentValues[] valuesArray) {
        if (trackId < 0 || segmentId < 0) {
            throw new IllegalArgumentException("Track and segments may not the less then 0.");
        }
        int inserted = 0;

        SQLiteDatabase sqldb = getWritableDatabase();
        sqldb.beginTransaction();
        try {
            for (ContentValues args : valuesArray) {
                args.put(Waypoints.SEGMENT, segmentId);

                long id = sqldb.insert(Waypoints.WAYPOINTS, null, args);
                if (id >= 0) {
                    inserted++;
                }
            }
            sqldb.setTransactionSuccessful();

        } finally {
            if (sqldb.inTransaction()) {
                sqldb.endTransaction();
            }
        }

        return inserted;
    }

    /**
     * Creates a waypoint under the current track segment with the current time
     * on which the waypoint is reached
     *
     * @return
     */
    long insertWaypoint(long trackId, long segmentId, Location location) {
        if (trackId < 0 || segmentId < 0) {
            throw new IllegalArgumentException("Track and segments may not the less then 0.");
        }

        SQLiteDatabase sqldb = getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(WaypointsColumns.SEGMENT, segmentId);
        args.put(WaypointsColumns.TIME, location.getTime());
        args.put(WaypointsColumns.LATITUDE, location.getLatitude());
        args.put(WaypointsColumns.LONGITUDE, location.getLongitude());
        args.put(WaypointsColumns.SPEED, location.getSpeed());
        args.put(WaypointsColumns.ACCURACY, location.getAccuracy());
        args.put(WaypointsColumns.ALTITUDE, location.getAltitude());
        args.put(WaypointsColumns.BEARING, location.getBearing());

        long waypointId = sqldb.insert(Waypoints.WAYPOINTS, null, args);

        ContentResolver resolver = this.mContext.getContentResolver();
        Uri notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/waypoints");
        resolver.notifyChange(notifyUri, null);

        //      Log.d( TAG, "Waypoint stored: "+notifyUri);
        return waypointId;
    }

    /**
     * Insert a URI for a given waypoint/segment/track in the media table
     *
     * @param trackId
     * @param segmentId
     * @param waypointId
     * @param mediaUri
     * @return
     */
    long insertMedia(long trackId, long segmentId, long waypointId, String mediaUri) {
        if (trackId < 0 || segmentId < 0 || waypointId < 0) {
            throw new IllegalArgumentException("Track, segments and waypoint may not the less then 0.");
        }
        SQLiteDatabase sqldb = getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(MediaColumns.TRACK, trackId);
        args.put(MediaColumns.SEGMENT, segmentId);
        args.put(MediaColumns.WAYPOINT, waypointId);
        args.put(MediaColumns.URI, mediaUri);

        //      Log.d( TAG, "Media stored in the datebase: "+mediaUri );

        long mediaId = sqldb.insert(Media.MEDIA, null, args);

        ContentResolver resolver = this.mContext.getContentResolver();
        Uri notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/waypoints/" +
                waypointId + "/media");
        resolver.notifyChange(notifyUri, null);
        //      Log.d( TAG, "Notify: "+notifyUri );
        resolver.notifyChange(Media.MEDIA_URI, null);
        //      Log.d( TAG, "Notify: "+Media.CONTENT_URI );

        return mediaId;
    }

    /**
     * Insert a key/value pair as meta-data for a track and optionally narrow the
     * scope by segment or segment/waypoint
     *
     * @param trackId
     * @param segmentId
     * @param waypointId
     * @param key
     * @param value
     * @return
     */
    long insertOrUpdateMetaData(long trackId, long segmentId, long waypointId, String key, String value) {
        long metaDataId = -1;
        if (trackId < 0 && key != null && value != null) {
            throw new IllegalArgumentException("Track, key and value must be provided");
        }
        if (waypointId >= 0 && segmentId < 0) {
            throw new IllegalArgumentException("Waypoint must have segment");
        }

        ContentValues args = new ContentValues();
        args.put(MetaData.TRACK, trackId);
        args.put(MetaData.SEGMENT, segmentId);
        args.put(MetaData.WAYPOINT, waypointId);
        args.put(MetaData.KEY, key);
        args.put(MetaData.VALUE, value);
        String whereClause = MetaData.TRACK + " = ? AND " + MetaData.SEGMENT + " = ? AND " + MetaData.WAYPOINT + " = ? " +
                "AND " + MetaData.KEY + " = ?";
        String[] whereArgs = new String[]{Long.toString(trackId), Long.toString(segmentId), Long.toString(waypointId)
                , key};

        SQLiteDatabase sqldb = getWritableDatabase();
        int updated = sqldb.update(MetaData.METADATA, args, whereClause, whereArgs);
        if (updated == 0) {
            metaDataId = sqldb.insert(MetaData.METADATA, null, args);
        } else {
            Cursor c = null;
            try {
                c = sqldb.query(MetaData.METADATA, new String[]{MetaData._ID}, whereClause, whereArgs, null, null, null);
                if (c.moveToFirst()) {
                    metaDataId = c.getLong(0);
                }
            } finally {
                if (c != null) {
                    c.close();
                }
            }
        }

        ContentResolver resolver = this.mContext.getContentResolver();
        Uri notifyUri;
        if (segmentId >= 0 && waypointId >= 0) {
            notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/waypoints/" +
                    waypointId + "/metadata");
        } else if (segmentId >= 0) {
            notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/metadata");
        } else {
            notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/metadata");
        }
        resolver.notifyChange(notifyUri, null);
        resolver.notifyChange(MetaData.METADATA_URI, null);

        return metaDataId;
    }

    /**
     * Deletes a single track and all underlying segments, waypoints, media and
     * metadata
     *
     * @param trackId
     * @return
     */
    int deleteTrack(long trackId) {
        SQLiteDatabase sqldb = getWritableDatabase();
        int affected = 0;
        Cursor cursor = null;
        long segmentId = -1;
        long metadataId = -1;

        try {
            sqldb.beginTransaction();
            // Iterate on each segement to delete each
            cursor = sqldb.query(Segments.SEGMENTS, new String[]{Segments._ID}, Segments.TRACK + "= ?", new String[]{
                            String.valueOf(trackId)}, null, null,
                    null, null);
            if (cursor.moveToFirst()) {
                do {
                    segmentId = cursor.getLong(0);
                    affected += deleteSegment(sqldb, trackId, segmentId);
                }
                while (cursor.moveToNext());
            } else {
                Timber.e("Did not find the last active segment");
            }
            // Delete the track
            affected += sqldb.delete(Tracks.TRACKS, Tracks._ID + "= ?", new String[]{String.valueOf(trackId)});
            // Delete remaining meta-data
            affected += sqldb.delete(MetaData.METADATA, MetaData.TRACK + "= ?", new String[]{String.valueOf(trackId)});

            cursor = sqldb.query(MetaData.METADATA, new String[]{MetaData._ID}, MetaData.TRACK + "= ?", new String[]{
                            String.valueOf(trackId)}, null, null,
                    null, null);
            if (cursor.moveToFirst()) {
                do {
                    metadataId = cursor.getLong(0);
                    affected += deleteMetaData(metadataId);
                }
                while (cursor.moveToNext());
            }

            sqldb.setTransactionSuccessful();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (sqldb.inTransaction()) {
                sqldb.endTransaction();
            }
        }

        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.notifyChange(Tracks.TRACKS_URI, null);
        resolver.notifyChange(ContentUris.withAppendedId(Tracks.TRACKS_URI, trackId), null);

        return affected;
    }

    /**
     * Delete a segment and all member waypoints
     *
     * @param sqldb     The SQLiteDatabase in question
     * @param trackId   The track id of this delete
     * @param segmentId The segment that needs deleting
     * @return
     */
    int deleteSegment(SQLiteDatabase sqldb, long trackId, long segmentId) {
        int affected = sqldb.delete(Segments.SEGMENTS, Segments._ID + "= ?", new String[]{String.valueOf(segmentId)});

        // Delete all waypoints from segments
        affected += sqldb.delete(Waypoints.WAYPOINTS, Waypoints.SEGMENT + "= ?", new String[]{String.valueOf(segmentId)});
        // Delete all media from segment
        affected += sqldb.delete(Media.MEDIA, Media.TRACK + "= ? AND " + Media.SEGMENT + "= ?",
                new String[]{String.valueOf(trackId), String.valueOf(segmentId)});
        // Delete meta-data
        affected += sqldb.delete(MetaData.METADATA, MetaData.TRACK + "= ? AND " + MetaData.SEGMENT + "= ?",
                new String[]{String.valueOf(trackId), String.valueOf(segmentId)});

        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.notifyChange(Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId), null);
        resolver.notifyChange(Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments"), null);

        return affected;
    }

    int deleteMetaData(long metadataId) {
        SQLiteDatabase sqldb = getWritableDatabase();

        Cursor cursor = null;
        long trackId = -1;
        long segmentId = -1;
        long waypointId = -1;
        try {
            cursor = sqldb.query(MetaData.METADATA, new String[]{MetaData.TRACK, MetaData.SEGMENT, MetaData.WAYPOINT},
                    MetaData._ID + "= ?",
                    new String[]{String.valueOf(metadataId)}, null, null, null, null);
            if (cursor.moveToFirst()) {
                trackId = cursor.getLong(0);
                segmentId = cursor.getLong(0);
                waypointId = cursor.getLong(0);
            } else {
                Timber.e("Did not find the media element to delete");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        int affected = sqldb.delete(MetaData.METADATA, MetaData._ID + "= ?", new String[]{String.valueOf(metadataId)});

        ContentResolver resolver = this.mContext.getContentResolver();
        Uri notifyUri;
        if (trackId >= 0 && segmentId >= 0 && waypointId >= 0) {
            notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/waypoints/" +
                    waypointId + "/media");
            resolver.notifyChange(notifyUri, null);
        }
        if (trackId >= 0 && segmentId >= 0) {
            notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/media");
            resolver.notifyChange(notifyUri, null);
        }
        notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/media");
        resolver.notifyChange(notifyUri, null);
        resolver.notifyChange(ContentUris.withAppendedId(Media.MEDIA_URI, metadataId), null);

        return affected;
    }

    /**
     * @param mediaId
     * @return
     */
    int deleteMedia(long mediaId) {
        SQLiteDatabase sqldb = getWritableDatabase();

        Cursor cursor = null;
        long trackId = -1;
        long segmentId = -1;
        long waypointId = -1;
        try {
            cursor = sqldb.query(Media.MEDIA, new String[]{Media.TRACK, Media.SEGMENT, Media.WAYPOINT}, Media._ID +
                            "= ?",
                    new String[]{String.valueOf(mediaId)}, null, null, null, null);
            if (cursor.moveToFirst()) {
                trackId = cursor.getLong(0);
                segmentId = cursor.getLong(0);
                waypointId = cursor.getLong(0);
            } else {
                Timber.e("Did not find the media element to delete");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        int affected = sqldb.delete(Media.MEDIA, Media._ID + "= ?", new String[]{String.valueOf(mediaId)});

        ContentResolver resolver = this.mContext.getContentResolver();
        Uri notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/waypoints/" +
                waypointId + "/media");
        resolver.notifyChange(notifyUri, null);
        notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/media");
        resolver.notifyChange(notifyUri, null);
        notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/media");
        resolver.notifyChange(notifyUri, null);
        resolver.notifyChange(ContentUris.withAppendedId(Media.MEDIA_URI, mediaId), null);

        return affected;
    }

    int updateTrack(long trackId, String name) {
        int updates;
        String whereclause = Tracks._ID + " = " + trackId;
        ContentValues args = new ContentValues();
        args.put(Tracks.NAME, name);

        // Execute the query.
        SQLiteDatabase mDb = getWritableDatabase();
        updates = mDb.update(Tracks.TRACKS, args, whereclause, null);

        ContentResolver resolver = this.mContext.getContentResolver();
        Uri notifyUri = ContentUris.withAppendedId(Tracks.TRACKS_URI, trackId);
        resolver.notifyChange(notifyUri, null);

        return updates;
    }

    /**
     * Insert a key/value pair as meta-data for a track and optionally narrow the
     * scope by segment or segment/waypoint
     *
     */
    int updateMetaData(long trackId, long segmentId, long waypointId, long metadataId, String selection, String[]
            selectionArgs, String value) {
        {
            if ((metadataId < 0 && trackId < 0)) {
                throw new IllegalArgumentException("Track or meta-data id be provided");
            }
            if (trackId >= 0 && (selection == null || !selection.contains("?") || selectionArgs.length != 1)) {
                throw new IllegalArgumentException("A where clause selection must be provided to select the correct KEY");
            }
            if (trackId >= 0 && waypointId >= 0 && segmentId < 0) {
                throw new IllegalArgumentException("Waypoint must have segment");
            }

            SQLiteDatabase sqldb = getWritableDatabase();

            String[] whereParams;
            String whereclause;
            if (metadataId >= 0) {
                whereclause = MetaData._ID + " = ? ";
                whereParams = new String[]{Long.toString(metadataId)};
            } else {
                whereclause = MetaData.TRACK + " = ? AND " + MetaData.SEGMENT + " = ? AND " + MetaData.WAYPOINT + " = ? " +
                        "AND " + MetaData.KEY + " = ? ";
                whereParams = new String[]{Long.toString(trackId), Long.toString(segmentId), Long.toString(waypointId),
                        selectionArgs[0]};
            }
            ContentValues args = new ContentValues();
            args.put(MetaData.VALUE, value);

            int updates = sqldb.update(MetaData.METADATA, args, whereclause, whereParams);

            ContentResolver resolver = this.mContext.getContentResolver();
            Uri notifyUri;
            if (trackId >= 0 && segmentId >= 0 && waypointId >= 0) {
                notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/waypoints/" +
                        waypointId + "/metadata");
            } else if (trackId >= 0 && segmentId >= 0) {
                notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments/" + segmentId + "/metadata");
            } else if (trackId >= 0) {
                notifyUri = Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/metadata");
            } else {
                notifyUri = Uri.withAppendedPath(MetaData.METADATA_URI, "" + metadataId);
            }

            resolver.notifyChange(notifyUri, null);
            resolver.notifyChange(MetaData.METADATA_URI, null);

            return updates;
        }
    }

    /**
     * Move to a fresh track with a new first segment for this track
     *
     */
    long toNextTrack(String name) {
        long currentTime = new Date().getTime();
        ContentValues args = new ContentValues();
        args.put(TracksColumns.NAME, name);
        args.put(TracksColumns.CREATION_TIME, currentTime);

        SQLiteDatabase sqldb = getWritableDatabase();
        long trackId = sqldb.insert(Tracks.TRACKS, null, args);

        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.notifyChange(Tracks.TRACKS_URI, null);

        return trackId;
    }

    /**
     * Moves to a fresh segment to which waypoints can be connected
     *
     */
    long toNextSegment(long trackId) {
        SQLiteDatabase sqldb = getWritableDatabase();

        ContentValues args = new ContentValues();
        args.put(Segments.TRACK, trackId);
        long segmentId = sqldb.insert(Segments.SEGMENTS, null, args);

        ContentResolver resolver = this.mContext.getContentResolver();
        resolver.notifyChange(Uri.withAppendedPath(Tracks.TRACKS_URI, trackId + "/segments"), null);

        return segmentId;
    }
}
