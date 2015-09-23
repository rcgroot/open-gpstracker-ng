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
package nl.sogeti.android.gpstracker.db;

import android.content.ContentUris;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.BaseColumns;

import nl.sogeti.android.gpstracker.BuildConfig;

/**
 * The GPStracking provider stores all static information about GPStracking.
 *
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 * @version $Id$
 */
public final class GPStracking {
    /**
     * The content:// style Uri for this provider, content://nl.sogeti.android.gpstracker
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + BuildConfig.CONTENT_AUTHORITY);
    /**
     * The name of the database file
     */
    static final String DATABASE_NAME = "GPSLOG.db";
    /**
     * The version of the database schema
     */
    static final int DATABASE_VERSION = 10;

    /**
     * This table contains tracks.
     *
     * @author rene
     */
    public static final class Tracks extends TracksColumns implements BaseColumns {
        /**
         * The MIME type of a CONTENT_URI subdirectory of a single track.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nl.sogeti.android.track";
        /**
         * The MIME type of CONTENT_URI providing a directory of tracks.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nl.sogeti.android.track";
        /**
         * The name of this table
         */
        public static final String TABLE = "tracks";
        /**
         * The content:// style URL for this provider, content://nl.sogeti.android.gpstracker/tracks
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + BuildConfig.CONTENT_AUTHORITY + "/" + Tracks.TABLE);
        static final String CREATE_STATEMENT =
                "CREATE TABLE " + Tracks.TABLE + "(" + " " + Tracks._ID + " " + Tracks._ID_TYPE +
                        "," + " " + Tracks.NAME + " " + Tracks.NAME_TYPE +
                        "," + " " + Tracks.CREATION_TIME + " " + Tracks.CREATION_TIME_TYPE +
                        ");";
    }

    /**
     * This table contains segments.
     *
     * @author rene
     */
    public static final class Segments extends SegmentsColumns implements BaseColumns {

        /**
         * The MIME type of a CONTENT_URI subdirectory of a single segment.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nl.sogeti.android.segment";
        /**
         * The MIME type of CONTENT_URI providing a directory of segments.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nl.sogeti.android.segment";

        /**
         * The name of this table, segments
         */
        public static final String TABLE = "segments";
        static final String CREATE_STATMENT =
                "CREATE TABLE " + Segments.TABLE + "(" + " " + Segments._ID + " " + Segments._ID_TYPE +
                        "," + " " + Segments.TRACK + " " + Segments.TRACK_TYPE +
                        ");";
    }

    /**
     * This table contains waypoints.
     *
     * @author rene
     */
    public static final class Waypoints extends WaypointsColumns implements BaseColumns {

        /**
         * The MIME type of a CONTENT_URI subdirectory of a single waypoint.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nl.sogeti.android.waypoint";
        /**
         * The MIME type of CONTENT_URI providing a directory of waypoints.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nl.sogeti.android.waypoint";

        /**
         * The name of this table, waypoints
         */
        public static final String TABLE = "waypoints";
        static final String CREATE_STATEMENT = "CREATE TABLE " + Waypoints.TABLE +
                "(" + " " + BaseColumns._ID + " " + WaypointsColumns._ID_TYPE +
                "," + " " + WaypointsColumns.LATITUDE + " " + WaypointsColumns.LATITUDE_TYPE +
                "," + " " + WaypointsColumns.LONGITUDE + " " + WaypointsColumns.LONGITUDE_TYPE +
                "," + " " + WaypointsColumns.TIME + " " + WaypointsColumns.TIME_TYPE +
                "," + " " + WaypointsColumns.SPEED + " " + WaypointsColumns.SPEED +
                "," + " " + WaypointsColumns.SEGMENT + " " + WaypointsColumns.SEGMENT_TYPE +
                "," + " " + WaypointsColumns.ACCURACY + " " + WaypointsColumns.ACCURACY_TYPE +
                "," + " " + WaypointsColumns.ALTITUDE + " " + WaypointsColumns.ALTITUDE_TYPE +
                "," + " " + WaypointsColumns.BEARING + " " + WaypointsColumns.BEARING_TYPE +
                ");";

        static final String[] UPGRADE_STATEMENT_7_TO_8 =
                {
                        "ALTER TABLE " + Waypoints.TABLE + " ADD COLUMN " + WaypointsColumns.ACCURACY + " " + WaypointsColumns.ACCURACY_TYPE + ";",
                        "ALTER TABLE " + Waypoints.TABLE + " ADD COLUMN " + WaypointsColumns.ALTITUDE + " " + WaypointsColumns.ALTITUDE_TYPE + ";",
                        "ALTER TABLE " + Waypoints.TABLE + " ADD COLUMN " + WaypointsColumns.BEARING + " " + WaypointsColumns.BEARING_TYPE + ";"
                };

        /**
         * Build a waypoint Uri like:
         * content://nl.sogeti.android.gpstracker/tracks/2/segments/1/waypoints/52
         * using the provided identifiers
         *
         * @param trackId
         * @param segmentId
         * @param waypointId
         * @return
         */
        public static Uri buildUri(long trackId, long segmentId, long waypointId) {
            Builder builder = Tracks.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, trackId);
            builder.appendPath(Segments.TABLE);
            ContentUris.appendId(builder, segmentId);
            builder.appendPath(Waypoints.TABLE);
            ContentUris.appendId(builder, waypointId);

            return builder.build();
        }
    }

    /**
     * This table contains media URI's.
     *
     * @author rene
     */
    public static final class Media extends MediaColumns implements BaseColumns {

        /**
         * The MIME type of a CONTENT_URI subdirectory of a single media entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nl.sogeti.android.media";
        /**
         * The MIME type of CONTENT_URI providing a directory of media entry.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nl.sogeti.android.media";

        /**
         * The name of this table
         */
        public static final String TABLE = "media";
        public static final Uri CONTENT_URI = Uri.parse("content://" + BuildConfig.CONTENT_AUTHORITY + "/" + Media.TABLE);
        static final String CREATE_STATEMENT = "CREATE TABLE " + Media.TABLE +
                "(" + " " + BaseColumns._ID + " " + MediaColumns._ID_TYPE +
                "," + " " + MediaColumns.TRACK + " " + MediaColumns.TRACK_TYPE +
                "," + " " + MediaColumns.SEGMENT + " " + MediaColumns.SEGMENT_TYPE +
                "," + " " + MediaColumns.WAYPOINT + " " + MediaColumns.WAYPOINT_TYPE +
                "," + " " + MediaColumns.URI + " " + MediaColumns.URI_TYPE +
                ");";
    }

    /**
     * This table contains media URI's.
     *
     * @author rene
     */
    public static final class MetaData extends MetaDataColumns implements BaseColumns {

        /**
         * The MIME type of a CONTENT_URI subdirectory of a single metadata entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.nl.sogeti.android.metadata";
        /**
         * The MIME type of CONTENT_URI providing a directory of media entry.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.nl.sogeti.android.metadata";

        /**
         * The name of this table
         */
        public static final String TABLE = "metadata";
        /**
         * content://nl.sogeti.android.gpstracker/metadata
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + BuildConfig.CONTENT_AUTHORITY + "/" + MetaData.TABLE);
        static final String CREATE_STATEMENT = "CREATE TABLE " + MetaData.TABLE +
                "(" + " " + BaseColumns._ID + " " + MetaDataColumns._ID_TYPE +
                "," + " " + MetaDataColumns.TRACK + " " + MetaDataColumns.TRACK_TYPE +
                "," + " " + MetaDataColumns.SEGMENT + " " + MetaDataColumns.SEGMENT_TYPE +
                "," + " " + MetaDataColumns.WAYPOINT + " " + MetaDataColumns.WAYPOINT_TYPE +
                "," + " " + MetaDataColumns.KEY + " " + MetaDataColumns.KEY_TYPE +
                "," + " " + MetaDataColumns.VALUE + " " + MetaDataColumns.VALUE_TYPE +
                ");";
    }

    /**
     * Columns from the tracks table.
     *
     * @author rene
     */
    public static class TracksColumns {
        public static final String NAME = "name";
        public static final String CREATION_TIME = "creationtime";
        static final String CREATION_TIME_TYPE = "INTEGER NOT NULL";
        static final String NAME_TYPE = "TEXT";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    }

    /**
     * Columns from the segments table.
     *
     * @author rene
     */
    public static class SegmentsColumns {
        /**
         * The track _id to which this segment belongs
         */
        public static final String TRACK = "track";
        static final String TRACK_TYPE = "INTEGER NOT NULL";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    }

    /**
     * Columns from the waypoints table.
     *
     * @author rene
     */
    public static class WaypointsColumns {
        /**
         * The latitude
         */
        public static final String LATITUDE = "latitude";
        /**
         * The longitude
         */
        public static final String LONGITUDE = "longitude";
        /**
         * The recorded time
         */
        public static final String TIME = "time";
        /**
         * The speed in meters per second
         */
        public static final String SPEED = "speed";
        /**
         * The segment _id to which this segment belongs
         */
        public static final String SEGMENT = "tracksegment";
        /**
         * The accuracy of the fix
         */
        public static final String ACCURACY = "accuracy";
        /**
         * The altitude
         */
        public static final String ALTITUDE = "altitude";
        /**
         * the bearing of the fix
         */
        public static final String BEARING = "bearing";

        static final String LATITUDE_TYPE = "REAL NOT NULL";
        static final String LONGITUDE_TYPE = "REAL NOT NULL";
        static final String TIME_TYPE = "INTEGER NOT NULL";
        static final String SPEED_TYPE = "REAL NOT NULL";
        static final String SEGMENT_TYPE = "INTEGER NOT NULL";
        static final String ACCURACY_TYPE = "REAL";
        static final String ALTITUDE_TYPE = "REAL";
        static final String BEARING_TYPE = "REAL";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    }

    /**
     * Columns from the media table.
     *
     * @author rene
     */
    public static class MediaColumns {
        /**
         * The track _id to which this segment belongs
         */
        public static final String TRACK = "track";
        public static final String SEGMENT = "segment";
        public static final String WAYPOINT = "waypoint";
        public static final String URI = "uri";
        static final String TRACK_TYPE = "INTEGER NOT NULL";
        static final String SEGMENT_TYPE = "INTEGER NOT NULL";
        static final String WAYPOINT_TYPE = "INTEGER NOT NULL";
        static final String URI_TYPE = "TEXT";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    }

    /**
     * Columns from the media table.
     *
     * @author rene
     */
    public static class MetaDataColumns {
        /**
         * The track _id to which this segment belongs
         */
        public static final String TRACK = "track";
        public static final String SEGMENT = "segment";
        public static final String WAYPOINT = "waypoint";
        public static final String KEY = "key";
        public static final String VALUE = "value";
        static final String TRACK_TYPE = "INTEGER NOT NULL";
        static final String SEGMENT_TYPE = "INTEGER";
        static final String WAYPOINT_TYPE = "INTEGER";
        static final String KEY_TYPE = "TEXT NOT NULL";
        static final String VALUE_TYPE = "TEXT NOT NULL";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
    }
}
