/*------------------------------------------------------------------------------
 **     Ident: Sogeti Mobile Solutions
 **    Author: rene
 ** Copyright: (c) Feb 11, 2016 Sogeti Nederland B.V. All Rights Reserved.
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

import android.provider.BaseColumns;

import nl.sogeti.android.gpstracker.service.integration.ContentConstants;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.MediaColumns;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.MetaDataColumns;
import nl.sogeti.android.gpstracker.service.integration.ContentConstants.WaypointsColumns;

public class DatabaseConstants {
    /**
     * The name of the database file
     */
    static final String DATABASE_NAME = "GPSLOG.db";
    /**
     * The version of the database schema
     */
    static final int DATABASE_VERSION = 10;

    public static final class Tracks extends ContentConstants.Tracks {
        static final String CREATION_TIME_TYPE = "INTEGER NOT NULL";
        static final String NAME_TYPE = "TEXT";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
        static final String CREATE_STATEMENT =
                "CREATE TABLE " + Tracks.TRACKS + "(" + " " + Tracks._ID + " " + _ID_TYPE +
                        "," + " " + Tracks.NAME + " " + NAME_TYPE +
                        "," + " " + Tracks.CREATION_TIME + " " + CREATION_TIME_TYPE +
                        ");";
    }

    public static final class Segments extends ContentConstants.Segments {
        static final String TRACK_TYPE = "INTEGER NOT NULL";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
        static final String CREATE_STATMENT =
                "CREATE TABLE " + Segments.SEGMENTS + "(" + " " + Segments._ID + " " + _ID_TYPE +
                        "," + " " + Segments.TRACK + " " + TRACK_TYPE +
                        ");";
    }

    public static final class Waypoints extends ContentConstants.Waypoints {
        static final String LATITUDE_TYPE = "REAL NOT NULL";
        static final String LONGITUDE_TYPE = "REAL NOT NULL";
        static final String TIME_TYPE = "INTEGER NOT NULL";
        static final String SPEED_TYPE = "REAL NOT NULL";
        static final String SEGMENT_TYPE = "INTEGER NOT NULL";
        static final String ACCURACY_TYPE = "REAL";
        static final String ALTITUDE_TYPE = "REAL";
        static final String BEARING_TYPE = "REAL";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
        static final String CREATE_STATEMENT = "CREATE TABLE " + Waypoints.WAYPOINTS +
                "(" + " " + BaseColumns._ID + " " + _ID_TYPE +
                "," + " " + WaypointsColumns.LATITUDE + " " + LATITUDE_TYPE +
                "," + " " + WaypointsColumns.LONGITUDE + " " + LONGITUDE_TYPE +
                "," + " " + WaypointsColumns.TIME + " " + TIME_TYPE +
                "," + " " + WaypointsColumns.SPEED + " " + SPEED +
                "," + " " + WaypointsColumns.SEGMENT + " " + SEGMENT_TYPE +
                "," + " " + WaypointsColumns.ACCURACY + " " + ACCURACY_TYPE +
                "," + " " + WaypointsColumns.ALTITUDE + " " + ALTITUDE_TYPE +
                "," + " " + WaypointsColumns.BEARING + " " + BEARING_TYPE +
                ");";

        static final String[] UPGRADE_STATEMENT_7_TO_8 =
                {
                        "ALTER TABLE " + Waypoints.WAYPOINTS + " ADD COLUMN " + WaypointsColumns.ACCURACY + " " +
                                ACCURACY_TYPE + ";",
                        "ALTER TABLE " + Waypoints.WAYPOINTS + " ADD COLUMN " + WaypointsColumns.ALTITUDE + " " +
                                ALTITUDE_TYPE + ";",
                        "ALTER TABLE " + Waypoints.WAYPOINTS + " ADD COLUMN " + WaypointsColumns.BEARING + " " +
                                BEARING_TYPE + ";"
                };
    }

    public static final class Media extends ContentConstants.Media {
        static final String TRACK_TYPE = "INTEGER NOT NULL";
        static final String SEGMENT_TYPE = "INTEGER NOT NULL";
        static final String WAYPOINT_TYPE = "INTEGER NOT NULL";
        static final String URI_TYPE = "TEXT";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
        static final String CREATE_STATEMENT = "CREATE TABLE " + Media.MEDIA +
                "(" + " " + BaseColumns._ID + " " + _ID_TYPE +
                "," + " " + MediaColumns.TRACK + " " + TRACK_TYPE +
                "," + " " + MediaColumns.SEGMENT + " " + SEGMENT_TYPE +
                "," + " " + MediaColumns.WAYPOINT + " " + WAYPOINT_TYPE +
                "," + " " + MediaColumns.URI + " " + URI_TYPE +
                ");";
    }

    public static final class MetaData extends ContentConstants.MetaData {
        static final String TRACK_TYPE = "INTEGER NOT NULL";
        static final String SEGMENT_TYPE = "INTEGER";
        static final String WAYPOINT_TYPE = "INTEGER";
        static final String KEY_TYPE = "TEXT NOT NULL";
        static final String VALUE_TYPE = "TEXT NOT NULL";
        static final String _ID_TYPE = "INTEGER PRIMARY KEY AUTOINCREMENT";
        static final String CREATE_STATEMENT = "CREATE TABLE " + MetaData.METADATA +
                "(" + " " + BaseColumns._ID + " " + _ID_TYPE +
                "," + " " + MetaDataColumns.TRACK + " " + TRACK_TYPE +
                "," + " " + MetaDataColumns.SEGMENT + " " + SEGMENT_TYPE +
                "," + " " + MetaDataColumns.WAYPOINT + " " + WAYPOINT_TYPE +
                "," + " " + MetaDataColumns.KEY + " " + KEY_TYPE +
                "," + " " + MetaDataColumns.VALUE + " " + VALUE_TYPE +
                ");";
    }
}
