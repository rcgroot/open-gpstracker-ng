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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import nl.sogeti.android.gpstracker.service.integration.ServiceConstants;


/**
 * Encapsulated persisted data used by during GPS Logging
 */
class LoggerPersistence {

    private static final String LOG_AT_BOOT = "LOG_AT_BOOT";
    private static final String LOG_AT_DOCK = "LOG_AT_DOCK";
    private static final String STOP_AT_UNDOCK = "STOP_AT_UNDOCK";
    private static final String LOG_AT_POWER_CONNECTED = "LOG_AT_POWER_CONNECTED";
    private static final String STOP_AT_POWER_DISCONNECTED = "STOP_AT_POWER_DISCONNECTED";
    private static final String BROADCAST_STREAM = "STREAM_ENABLED";
    private static final String LOGGING_INTERVAL_SECONDS = "LOGGING_INTERVAL_SECONDS";
    private static final String SPEED_SANITY_CHECK = "SPEED_SANITY_CHECK";
    private static final String LOGGING_DISTANCE = "LOGGING_DISTANCE";
    private static final String STREAM_BROADCAST_DISTANCE_METER = "STREAM_BROADCAST_DISTANCE_METER";
    private static final String STREAM_BROADCAST_TIME_MINUTES = "STREAM_BROADCAST_TIME_MINUTES";
    private static final String STATUS_MONITOR = "STATUS_MONITOR";
    private final Context context;

    public SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("LoggerPersistence", Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    private SharedPreferences sharedPreferences;

    LoggerPersistence(Context context) {
        this.context = context;
    }

    /* ***********************
     *
     * Service configuration
     */

    public void setPrecision(int currentPrecision) {
        put(LoggingConstants.SERVICESTATE_PRECISION, currentPrecision);
    }

    public int getPrecision() {
        return getSharedPreferences().getInt(LoggingConstants.SERVICESTATE_PRECISION, ServiceConstants.LOGGING_NORMAL);
    }

    public float getCustomLocationIntervalMetres() {
        return getSharedPreferences().getFloat(LOGGING_DISTANCE, 25.0f);
    }

    public void setCustomLocationIntervalMetres(float intervalMetres) {
        put(LOGGING_DISTANCE, intervalMetres);
    }

    public long getCustomLocationIntervalSeconds() {
        return getSharedPreferences().getLong(LOGGING_INTERVAL_SECONDS, 60L);
    }

    public void setCustomLocationIntervalSeconds(long interval) {
        put(LOGGING_INTERVAL_SECONDS, interval);
    }

    public boolean isSpeedChecked() {
        return getSharedPreferences().getBoolean(SPEED_SANITY_CHECK, true);
    }

    public void isSpeedChecked(boolean isChecked) {
        put(SPEED_SANITY_CHECK, isChecked);
    }

    public boolean isStatusMonitor() {
        return getSharedPreferences().getBoolean(STATUS_MONITOR, false);
    }

    public void isStatusMonitor(boolean isMonitor) {
        put(STATUS_MONITOR, isMonitor);
    }

    public boolean getStreamBroadcast() {
        return getSharedPreferences().getBoolean(BROADCAST_STREAM, false);
    }

    public void getStreamBroadcast(boolean isBroadcast) {
        put(BROADCAST_STREAM, isBroadcast);
    }

    public void setBroadcastIntervalMinutes(long minutes) {
        put(STREAM_BROADCAST_TIME_MINUTES, minutes);
    }

    public long getBroadcastIntervalMinutes() {
        return getSharedPreferences().getLong(STREAM_BROADCAST_TIME_MINUTES, 1L);
    }

    public void setBroadcastIntervalMeters(float meters) {
        put(STREAM_BROADCAST_DISTANCE_METER, meters);
    }

    public float getBroadcastIntervalMeters() {
        return getSharedPreferences().getFloat(STREAM_BROADCAST_DISTANCE_METER, 1L);
    }

    public boolean shouldLogAtBoot() {
        return getSharedPreferences().getBoolean(LOG_AT_BOOT, false);
    }

    public void shouldLogAtBoot(boolean value) {
        put(LOG_AT_BOOT, value);
    }

    public boolean shouldLogAtPowerConnected() {
        return getSharedPreferences().getBoolean(LOG_AT_POWER_CONNECTED, false);
    }

    public void shouldLogAtPowerConnected(boolean value) {
        put(LOG_AT_BOOT, value);
    }

    public boolean shouldLogAtPowerDisconnected() {
        return getSharedPreferences().getBoolean(STOP_AT_POWER_DISCONNECTED, false);
    }

    public void shouldLogAtPowerDisconnected(boolean value) {
        put(STOP_AT_POWER_DISCONNECTED, value);
    }

    public boolean shouldLogAtDockCar() {
        return getSharedPreferences().getBoolean(LOG_AT_DOCK, false);
    }

    public void shouldLogAtDockCar(boolean value) {
        put(LOG_AT_DOCK, value);
    }

    public boolean shouldLogAtUndockCar() {
        return getSharedPreferences().getBoolean(STOP_AT_UNDOCK, false);
    }

    public void shouldLogAtUndockCar(boolean value) {
        put(STOP_AT_UNDOCK, value);
    }

    /* ***********************
     *
     * Service crash protection
     */

    public void setTrackId(long currentTrackId) {
        put(LoggingConstants.SERVICESTATE_TRACKID, currentTrackId);
    }

    public long getTrackId() {
        return getSharedPreferences().getLong(LoggingConstants.SERVICESTATE_TRACKID, -1);
    }

    public void setSegmentId(long currentSegmentId) {
        put(LoggingConstants.SERVICESTATE_SEGMENTID, currentSegmentId);
    }

    public long getSegmentId() {
        return getSharedPreferences().getLong(LoggingConstants.SERVICESTATE_SEGMENTID, -1);
    }

    public void setDistance(float currentDistance) {
        put(LoggingConstants.SERVICESTATE_DISTANCE, currentDistance);
    }

    public float getDistance() {
        return getSharedPreferences().getFloat(LoggingConstants.SERVICESTATE_DISTANCE, 0F);
    }

    public void setLoggingState(int currentLoggingState) {
        put(LoggingConstants.SERVICESTATE_STATE, currentLoggingState);
    }

    public int getLoggingState() {
        return getSharedPreferences().getInt(LoggingConstants.SERVICESTATE_STATE, ServiceConstants.STATE_STOPPED);
    }

    /* *****
     * Helpers
     */

    private void put(String key, long value) {
        apply(getSharedPreferences().edit().putLong(key, value));
    }

    private void put(String key, int value) {
        apply(getSharedPreferences().edit().putInt(key, value));
    }

    private void put(String key, float value) {
        apply(getSharedPreferences().edit().putFloat(key, value));
    }

    private void put(String key, boolean value) {
        apply(getSharedPreferences().edit().putBoolean(key, value));
    }

    private void apply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
