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
package nl.sogeti.android.gpstracker.ng.sharing.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import nl.sogeti.android.gpstracker.integration.ContentConstants.Media;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Segments;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints;
import nl.sogeti.android.gpstracker.ng.common.GpsTrackerApplication;
import nl.sogeti.android.gpstracker.ng.utils.TrackUriExtensionKt;
import timber.log.Timber;

/**
 * Create a GPX version of a stored track
 *
 * @author rene (c) Mar 22, 2009, Sogeti B.V.
 * @version $Id$
 */
public class GpxCreator {
    private static final String NS_SCHEMA = "http://www.w3.org/2001/XMLSchema-instance";
    private static final String NS_GPX_11 = "http://www.topografix.com/GPX/1/1";
    private static final String NS_GPX_10 = "http://www.topografix.com/GPX/1/0";
    private static final String NS_OGT_10 = "http://gpstracker.android.sogeti.nl/GPX/1/0";
    private static final SimpleDateFormat ZULU_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        ZULU_DATE_FORMAT.setTimeZone(utc); // ZULU_DATE_FORMAT format ends with Z for UTC so make that true
    }

    private final Context context;
    private final Uri trackUri;
    private String mName = "Untitled";

    public GpxCreator(Context context, Uri trackUri) {
        this.context = context;
        this.trackUri = trackUri;
    }

    /**
     * @param outputStream will be wrapped with buffer not not closed
     */
    public void createGpx(OutputStream outputStream) {
        BufferedOutputStream buf = null;
        try {
            buf = new BufferedOutputStream(outputStream, 8 * 8192);
            serializeTrack(trackUri, buf);
        } catch (IllegalStateException | IOException e) {
            Timber.e(e, "Failed to export");
        } finally {
            if (buf != null) {
                try {
                    buf.close();
                } catch (IOException e) {
                    Timber.w(e, "Failed to close buf after completion, ignoring.");
                }
            }
        }
    }

    /**
     * Available to synchronously stream the URI to other targets then the default file in the task
     *
     * @param trackUri
     * @param outputStream
     * @throws IllegalArgumentException
     * @throws IllegalStateException
     * @throws IOException
     */
    private void serializeTrack(Uri trackUri, OutputStream outputStream) throws IllegalArgumentException,
            IllegalStateException, IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(outputStream, "UTF-8");
        serializer.startDocument("UTF-8", true);
        serializer.setPrefix("xsi", NS_SCHEMA);
        serializer.setPrefix("gpx10", NS_GPX_10);
        serializer.setPrefix("ogt10", NS_OGT_10);
        serializer.text("\n");
        serializer.startTag("", "gpx");
        serializer.attribute(null, "version", "1.1");
        serializer.attribute(null, "creator", "nl.sogeti.android.gpstracker");
        serializer.attribute(NS_SCHEMA, "schemaLocation", NS_GPX_11 + " http://www.topografix.com/gpx/1/1/gpx.xsd");
        serializer.attribute(null, "xmlns", NS_GPX_11);

        // <metadata/> Big header of the track
        serializeTrackHeader(context, serializer, trackUri);

        // Add waypoints with text notes
        serializeWaypoints(context, serializer, Uri.withAppendedPath(trackUri, "/media"));

        // <trk/> [0...] Track
        serializer.text("\n");
        serializer.startTag("", "trk");
        serializer.text("\n");
        serializer.startTag("", "name");
        serializer.text(mName);
        serializer.endTag("", "name");
        // The list of segments in the track
        serializeSegments(serializer, Uri.withAppendedPath(trackUri, "segments"));
        serializer.text("\n");
        serializer.endTag("", "trk");

        serializer.text("\n");
        serializer.endTag("", "gpx");
        serializer.endDocument();
    }

    private void serializeTrackHeader(Context context, XmlSerializer serializer, Uri trackUri) throws IOException {
        ContentResolver resolver = context.getContentResolver();
        Cursor trackCursor = null;

        String databaseName = null;
        try {
            trackCursor = resolver.query(trackUri, new String[]{Tracks._ID, Tracks.NAME, Tracks.CREATION_TIME}, null,
                    null, null);
            if (trackCursor.moveToFirst()) {
                databaseName = trackCursor.getString(1);
                serializer.text("\n");
                serializer.startTag("", "metadata");
                serializer.text("\n");
                serializer.startTag("", "time");
                Date time = new Date(trackCursor.getLong(2));
                synchronized (ZULU_DATE_FORMAT) {
                    serializer.text(ZULU_DATE_FORMAT.format(time));
                }
                serializer.endTag("", "time");
                serializer.text("\n");
                serializer.endTag("", "metadata");
            }
        } finally {
            if (trackCursor != null) {
                trackCursor.close();
            }
        }
        if (databaseName != null && !databaseName.equals("")) {
            mName = databaseName;
        }
    }

    private void serializeWaypoints(Context context, XmlSerializer serializer, Uri media) throws IOException {
        Cursor mediaCursor = null;
        Cursor waypointCursor = null;
        ContentResolver resolver = context.getContentResolver();
        try {
            mediaCursor = resolver.query(media, new String[]{Media.URI, Media.TRACK, Media.SEGMENT, Media.WAYPOINT},
                    null, null, null);
            if (mediaCursor != null && mediaCursor.moveToFirst()) {
                do {
                    Uri waypointUri = TrackUriExtensionKt.waypointUri(mediaCursor.getLong(1), mediaCursor.getLong(2), mediaCursor
                            .getLong(3));
                    waypointCursor = resolver.query(waypointUri, new String[]{Waypoints.LATITUDE, Waypoints.LONGITUDE,
                            Waypoints.ALTITUDE, Waypoints.TIME}, null, null, null);
                    serializer.text("\n");
                    serializer.startTag("", "wpt");
                    if (waypointCursor != null && waypointCursor.moveToFirst()) {
                        serializer.attribute(null, "lat", Double.toString(waypointCursor.getDouble(0)));
                        serializer.attribute(null, "lon", Double.toString(waypointCursor.getDouble(1)));
                        serializer.text("\n");
                        serializer.startTag("", "ele");
                        serializer.text(Double.toString(waypointCursor.getDouble(2)));
                        serializer.endTag("", "ele");
                        serializer.text("\n");
                        serializer.startTag("", "time");
                        Date time = new Date(waypointCursor.getLong(3));
                        synchronized (ZULU_DATE_FORMAT) {
                            serializer.text(ZULU_DATE_FORMAT.format(time));
                        }
                        serializer.endTag("", "time");
                    }
                    if (waypointCursor != null) {
                        waypointCursor.close();
                        waypointCursor = null;
                    }

                    Uri mediaUri = Uri.parse(mediaCursor.getString(0));
                    if (mediaUri.getScheme().equals("file")) {
                        if (mediaUri.getLastPathSegment().endsWith("txt")) {
                            quickTag(serializer, "", "name", mediaUri.getLastPathSegment());
                            serializer.startTag("", "desc");
                            BufferedReader buf = null;
                            try {
                                buf = new BufferedReader(new FileReader(mediaUri.getEncodedPath()));
                                String line;
                                while ((line = buf.readLine()) != null) {
                                    serializer.text(line);
                                    serializer.text("\n");
                                }
                            } finally {
                                serializer.endTag("", "desc");
                                if (buf != null) {
                                    buf.close();
                                }
                            }
                        }
                    } else if (mediaUri.getScheme().equals("content")) {
                        if ((GpsTrackerApplication.appComponent.providerAuthority() + ".string").equals(mediaUri.getAuthority())) {
                            quickTag(serializer, "", "name", mediaUri.getLastPathSegment());
                        }
                    }
                    serializer.text("\n");
                    serializer.endTag("", "wpt");
                }
                while (mediaCursor.moveToNext());
            }
        } finally {
            if (mediaCursor != null) {
                mediaCursor.close();
            }
            if (waypointCursor != null) {
                waypointCursor.close();
            }
        }
    }

    private void serializeSegments(XmlSerializer serializer, Uri segments) throws IOException {
        Cursor segmentCursor = null;
        ContentResolver resolver = context.getContentResolver();
        try {
            segmentCursor = resolver.query(segments, new String[]{Segments._ID}, null, null, null);
            if (segmentCursor != null && segmentCursor.moveToFirst()) {
                do {
                    Uri waypoints = Uri.withAppendedPath(segments, segmentCursor.getLong(0) + "/waypoints");
                    serializer.text("\n");
                    serializer.startTag("", "trkseg");
                    serializeTrackPoints(serializer, waypoints);
                    serializer.text("\n");
                    serializer.endTag("", "trkseg");
                }
                while (segmentCursor.moveToNext());
            }
        } finally {
            if (segmentCursor != null) {
                segmentCursor.close();
            }
        }
    }

    private void serializeTrackPoints(XmlSerializer serializer, Uri waypoints) throws IOException {
        Cursor waypointsCursor = null;
        ContentResolver resolver = context.getContentResolver();
        try {
            waypointsCursor = resolver.query(waypoints, new String[]{Waypoints.LONGITUDE, Waypoints.LATITUDE,
                    Waypoints.TIME, Waypoints.ALTITUDE, Waypoints._ID, Waypoints.SPEED, Waypoints.ACCURACY,
                    Waypoints.BEARING}, null, null, null);
            if (waypointsCursor != null && waypointsCursor.moveToFirst()) {
                do {
                    serializer.text("\n");
                    serializer.startTag("", "trkpt");
                    serializer.attribute(null, "lat", Double.toString(waypointsCursor.getDouble(1)));
                    serializer.attribute(null, "lon", Double.toString(waypointsCursor.getDouble(0)));
                    serializer.text("\n");
                    serializer.startTag("", "ele");
                    serializer.text(Double.toString(waypointsCursor.getDouble(3)));
                    serializer.endTag("", "ele");
                    serializer.text("\n");
                    serializer.startTag("", "time");
                    Date time = new Date(waypointsCursor.getLong(2));
                    synchronized (ZULU_DATE_FORMAT) {
                        serializer.text(ZULU_DATE_FORMAT.format(time));
                    }
                    serializer.endTag("", "time");
                    serializer.text("\n");
                    serializer.startTag("", "extensions");

                    double speed = waypointsCursor.getDouble(5);
                    double accuracy = waypointsCursor.getDouble(6);
                    double bearing = waypointsCursor.getDouble(7);
                    if (speed > 0.0) {
                        quickTag(serializer, NS_GPX_10, "speed", Double.toString(speed));
                    }
                    if (accuracy > 0.0) {
                        quickTag(serializer, NS_OGT_10, "accuracy", Double.toString(accuracy));
                    }
                    if (bearing != 0.0) {
                        quickTag(serializer, NS_GPX_10, "course", Double.toString(bearing));
                    }
                    serializer.endTag("", "extensions");
                    serializer.text("\n");
                    serializer.endTag("", "trkpt");
                }
                while (waypointsCursor.moveToNext());
            }
        } finally {
            if (waypointsCursor != null) {
                waypointsCursor.close();
            }
        }
    }

    private void quickTag(XmlSerializer serializer, String ns, String tag, String content)
            throws IllegalArgumentException, IllegalStateException, IOException {
        if (tag == null) {
            tag = "";
        }
        if (content == null) {
            content = "";
        }
        serializer.text("\n");
        serializer.startTag(ns, tag);
        serializer.text(content);
        serializer.endTag(ns, tag);
    }
}