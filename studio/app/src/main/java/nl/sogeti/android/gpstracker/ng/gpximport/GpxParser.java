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
package nl.sogeti.android.gpstracker.ng.gpximport;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Vector;

import nl.renedegroot.android.util.stream.UnicodeReader;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Tracks;
import nl.sogeti.android.gpstracker.integration.ContentConstants.Waypoints;
import timber.log.Timber;

import static nl.sogeti.android.gpstracker.ng.utils.TrackUriExtensionKt.tracksUri;

public class GpxParser {

    private static final SimpleDateFormat ZULU_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    private static final SimpleDateFormat ZULU_DATE_FORMAT_MS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
    private static final SimpleDateFormat ZULU_DATE_FORMAT_BC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US);
    private static final String LATITUDE_ATRIBUTE = "lat";
    private static final String LONGITUDE_ATTRIBUTE = "lon";
    private static final String TRACK_ELEMENT = "trkpt";
    private static final String SEGMENT_ELEMENT = "trkseg";
    private static final String NAME_ELEMENT = "name";
    private static final String TIME_ELEMENT = "time";
    private static final String ELEVATION_ELEMENT = "ele";
    private static final String COURSE_ELEMENT = "course";
    private static final String ACCURACY_ELEMENT = "accuracy";
    private static final String SPEED_ELEMENT = "speed";

    static {
        TimeZone utc = TimeZone.getTimeZone("UTC");
        ZULU_DATE_FORMAT.setTimeZone(utc); // ZULU_DATE_FORMAT format ends with Z for UTC so make that true
        ZULU_DATE_FORMAT_MS.setTimeZone(utc);
    }

    private final ContentResolver contentResolver;

    public GpxParser(Context context) {
        this.contentResolver = context.getContentResolver();
    }

    private static Long parseXmlDateTime(String text) {
        Long dateTime = 0L;
        try {
            if (text == null) {
                throw new ParseException("Unable to parse dateTime " + text + " of length ", 0);
            }
            int length = text.length();
            switch (length) {
                case 20:
                    synchronized (ZULU_DATE_FORMAT) {
                        dateTime = ZULU_DATE_FORMAT.parse(text).getTime();
                    }
                    break;
                case 23:
                    synchronized (ZULU_DATE_FORMAT_BC) {
                        dateTime = ZULU_DATE_FORMAT_BC.parse(text).getTime();
                    }
                    break;
                case 24:
                    synchronized (ZULU_DATE_FORMAT_MS) {
                        dateTime = ZULU_DATE_FORMAT_MS.parse(text).getTime();
                    }
                    break;
                default:
                    throw new ParseException("Unable to parse dateTime " + text + " of length " + length, 0);
            }
        } catch (ParseException e) {
            Timber.w(e, "Failed to parse a time-date");
        }

        return dateTime;
    }

    /**
     * Read a stream containing GPX XML into the OGT content provider
     *
     * @param inputStream opened stream the read from, will be closed after this call
     * @param defaultName
     * @return content provider uri
     */
    public Uri parseTrack(@NotNull InputStream inputStream, String defaultName) throws XmlPullParserException, IOException {
        Uri trackUri = null;
        int eventType;
        ContentValues lastPosition = null;
        Vector<ContentValues> bulk = new Vector<>();
        boolean speed = false;
        boolean accuracy = false;
        boolean bearing = false;
        boolean elevation = false;
        boolean name = false;
        boolean time = false;
        Long importDate = new Date().getTime();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xmlParser = factory.newPullParser();

        BufferedInputStream bis = new BufferedInputStream(inputStream);
        UnicodeReader ur = new UnicodeReader(bis, "UTF-8");
        xmlParser.setInput(ur);

        eventType = xmlParser.getEventType();
        String attributeName;
        Uri segmentUri = null;
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xmlParser.getName().equals(NAME_ELEMENT)) {
                    name = true;
                } else {
                    ContentValues trackContent = new ContentValues();
                    trackContent.put(Tracks.NAME, defaultName);
                    if (xmlParser.getName().equals("trk") && trackUri == null) {
                        trackUri = startTrack(trackContent);
                    } else if (xmlParser.getName().equals(SEGMENT_ELEMENT)) {
                        segmentUri = startSegment(trackUri);
                    } else if (xmlParser.getName().equals(TRACK_ELEMENT)) {
                        lastPosition = new ContentValues();
                        for (int i = 0; i < 2; i++) {
                            attributeName = xmlParser.getAttributeName(i);
                            if (attributeName.equals(LATITUDE_ATRIBUTE)) {
                                lastPosition.put(Waypoints.LATITUDE, Double.valueOf(xmlParser.getAttributeValue(i)));
                            } else if (attributeName.equals(LONGITUDE_ATTRIBUTE)) {
                                lastPosition.put(Waypoints.LONGITUDE, Double.valueOf(xmlParser.getAttributeValue(i)));
                            }
                        }
                    } else if (xmlParser.getName().equals(SPEED_ELEMENT)) {
                        speed = true;
                    } else if (xmlParser.getName().equals(ACCURACY_ELEMENT)) {
                        accuracy = true;
                    } else if (xmlParser.getName().equals(COURSE_ELEMENT)) {
                        bearing = true;
                    } else if (xmlParser.getName().equals(ELEVATION_ELEMENT)) {
                        elevation = true;
                    } else if (xmlParser.getName().equals(TIME_ELEMENT)) {
                        time = true;
                    }
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                if (xmlParser.getName().equals(NAME_ELEMENT)) {
                    name = false;
                } else if (xmlParser.getName().equals(SPEED_ELEMENT)) {
                    speed = false;
                } else if (xmlParser.getName().equals(ACCURACY_ELEMENT)) {
                    accuracy = false;
                } else if (xmlParser.getName().equals(COURSE_ELEMENT)) {
                    bearing = false;
                } else if (xmlParser.getName().equals(ELEVATION_ELEMENT)) {
                    elevation = false;
                } else if (xmlParser.getName().equals(TIME_ELEMENT)) {
                    time = false;
                } else if (xmlParser.getName().equals(SEGMENT_ELEMENT)) {
                    if (segmentUri == null) {
                        segmentUri = startSegment(trackUri);
                    }
                    contentResolver.bulkInsert(Uri.withAppendedPath(segmentUri, "waypoints"), bulk.toArray(new
                            ContentValues[bulk.size()]));
                    bulk.clear();
                } else if (xmlParser.getName().equals(TRACK_ELEMENT)) {
                    if (!lastPosition.containsKey(Waypoints.TIME)) {
                        lastPosition.put(Waypoints.TIME, importDate);
                    }
                    if (!lastPosition.containsKey(Waypoints.SPEED)) {
                        lastPosition.put(Waypoints.SPEED, 0);
                    }
                    if (lastPosition.containsKey(Waypoints.LATITUDE)
                            && lastPosition.containsKey(Waypoints.LONGITUDE)
                            && lastPosition.getAsDouble(Waypoints.LONGITUDE) != 0.0
                            && lastPosition.getAsDouble(Waypoints.LATITUDE) != 0.0) {
                        bulk.add(lastPosition);
                    }
                    lastPosition = null;
                }
            } else if (eventType == XmlPullParser.TEXT) {
                String text = xmlParser.getText();
                if (name) {
                    ContentValues nameValues = new ContentValues();
                    nameValues.put(Tracks.NAME, text);
                    if (trackUri == null) {
                        trackUri = startTrack(new ContentValues());
                    }
                    contentResolver.update(trackUri, nameValues, null, null);
                } else if (lastPosition != null && speed) {
                    lastPosition.put(Waypoints.SPEED, Double.parseDouble(text));
                } else if (lastPosition != null && accuracy) {
                    lastPosition.put(Waypoints.ACCURACY, Double.parseDouble(text));
                } else if (lastPosition != null && bearing) {
                    lastPosition.put(Waypoints.BEARING, Double.parseDouble(text));
                } else if (lastPosition != null && elevation) {
                    lastPosition.put(Waypoints.ALTITUDE, Double.parseDouble(text));
                } else if (lastPosition != null && time) {
                    lastPosition.put(Waypoints.TIME, parseXmlDateTime(text));
                }
            }
            eventType = xmlParser.next();
        }

        return trackUri;
    }

    private Uri startTrack(ContentValues trackContent) {
        return contentResolver.insert(tracksUri(), trackContent);
    }

    private Uri startSegment(Uri trackUri) {
        if (trackUri == null) {
            trackUri = startTrack(new ContentValues());
        }

        Uri segmentUri = Uri.withAppendedPath(trackUri, "segments");
        return contentResolver.insert(segmentUri, new ContentValues());
    }
}
