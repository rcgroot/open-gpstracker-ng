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
package nl.sogeti.android.gpstracker.integration;

import android.content.ContentUris;
import android.net.Uri;
import android.net.Uri.Builder;

/**
 * The GPSTracking provider stores all static information about GPSTracking.
 *
 * @author rene (c) Jan 22, 2009, Sogeti B.V.
 * @version $Id$
 */
public final class ContentConstants {
    /**
     * The authority of this provider: nl.sogeti.android.gpstracker
     */
    public static final String AUTHORITY = "nl.sogeti.android.gpstracker";
    /**
     * The content:// style Uri for this provider, content://nl.sogeti.android.gpstracker
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + ContentConstants.AUTHORITY);

    /**
     * Build a waypoint Uri like:
     * content://nl.sogeti.android.gpstracker/tracks/2/segments/1/waypoints/52
     * using the provided identifiers
     *
     * @param trackId identifier denoting the track
     * @param segmentId identifier denoting the segment
     * @param waypointId identifier denoting the waypoint
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

    /**
     * Build a waypoint Uri like:
     * content://nl.sogeti.android.gpstracker/tracks/2/segments/1/waypoints
     * using the provided identifiers
     *
     * @param trackId identifier denoting the track
     * @param segmentId identifier denoting the segment
     * @return
     */
    public static Uri buildUri(long trackId, long segmentId) {
        Builder builder = Tracks.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, trackId);
        builder.appendPath(Segments.TABLE);
        ContentUris.appendId(builder, segmentId);
        builder.appendPath(Waypoints.TABLE);

        return builder.build();
    }

    /**
     * This table contains tracks.
     */
    public static class Tracks extends TracksColumns implements android.provider.BaseColumns {
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
        public static final Uri CONTENT_URI = Uri.parse("content://" + ContentConstants.AUTHORITY + "/" + Tracks.TABLE);

    }

    /**
     * This table contains segments.
     */
    public static class Segments extends SegmentsColumns implements android.provider.BaseColumns {

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
    }

    /**
     * This table contains waypoints.
     */
    public static class Waypoints extends WaypointsColumns implements android.provider.BaseColumns {

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


    }

    /**
     * This table contains media URI's.
     */
    public static class Media extends MediaColumns implements android.provider.BaseColumns {

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
        public static final Uri CONTENT_URI = Uri.parse("content://" + ContentConstants.AUTHORITY + "/" + Media.TABLE);

    }

    /**
     * This table contains media URI's.
     */
    public static class MetaData extends MetaDataColumns implements android.provider.BaseColumns {

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
        public static final Uri CONTENT_URI = Uri.parse("content://" + ContentConstants.AUTHORITY + "/" + MetaData.TABLE);
    }

    /**
     * Columns from the tracks table.
     */
    public static class TracksColumns {
        public static final String NAME = "name";
        public static final String CREATION_TIME = "creationtime";
    }

    /**
     * Columns from the segments table.
     */
    public static class SegmentsColumns {
        /**
         * The track _id to which this segment belongs
         */
        public static final String TRACK = "track";
    }

    /**
     * Columns from the waypoints table.
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
    }

    /**
     * Columns from the media table.
     */
    public static class MediaColumns {
        /**
         * The track _id to which this segment belongs
         */
        public static final String TRACK = "track";
        public static final String SEGMENT = "segment";
        public static final String WAYPOINT = "waypoint";
        public static final String URI = "uri";
    }

    /**
     * Columns from the media table.
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
    }
}
