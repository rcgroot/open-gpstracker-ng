package nl.sogeti.android.gpstracker.logger;

import nl.sogeti.android.gpstracker.map.TrackMapActivity;

/**
 * Created by grootren on 26-08-15.
 */
public class Constants {

    public static final String SERVICENAME = "nl.sogeti.android.gpstracker.intent.action.GPSLoggerService";

    /**
     * Broadcast intent action indicating that the logger service state has changed. Includes the logging state and its precision.
     *
     * @see #EXTRA_LOGGING_PRECISION
     * @see #EXTRA_LOGGING_STATE
     */
    public static final String LOGGING_STATE_CHANGED_ACTION = "nl.sogeti.android.gpstracker.LOGGING_STATE_CHANGED";

    /**
     * The precision the service is logging on.
     *
     * @see #LOGGING_FINE
     * @see #LOGGING_NORMAL
     * @see #LOGGING_COARSE
     * @see #LOGGING_GLOBAL
     * @see #LOGGING_CUSTOM
     */
    public static final String EXTRA_LOGGING_PRECISION = "nl.sogeti.android.gpstracker.EXTRA_LOGGING_PRECISION";

    /**
     * The state the service is.
     *
     * @see #UNKNOWN
     * @see #LOGGING
     * @see #PAUSED
     * @see #STOPPED
     */
    public static final String EXTRA_LOGGING_STATE = "nl.sogeti.android.gpstracker.EXTRA_LOGGING_STATE";


    /**
     * The state of the service is unknown
     */
    public static final int UNKNOWN = -1;

    /**
     * The service is actively logging, it has requested location update from the location provider.
     */
    public static final int LOGGING = 1;

    /**
     * The service is not active, but can be resumed to become active and store location changes as part of a new segment of the current track.
     */
    public static final int PAUSED = 2;

    /**
     * The service is not active and can not resume a current track but must start a new one when becoming active.
     */
    public static final int STOPPED = 3;

    /**
     * The precision of the GPS provider is based on the custom time interval and distance.
     */
    public static final int LOGGING_CUSTOM = 0;

    /**
     * The GPS location provider is asked to update every 10 seconds or every 5 meters.
     */
    public static final int LOGGING_FINE = 1;

    /**
     * The GPS location provider is asked to update every 15 seconds or every 10 meters.
     */
    public static final int LOGGING_NORMAL = 2;

    /**
     * The GPS location provider is asked to update every 30 seconds or every 25 meters.
     */
    public static final int LOGGING_COARSE = 3;

    /**
     * The radio location provider is asked to update every 5 minutes or every 500 meters.
     */
    public static final int LOGGING_GLOBAL = 4;

    /**
     * Preference key for logging precision
     */
    public static final String PRECISION = "precision";
    /**
     * Preference key for logging custom precision time
     */
    public static final String LOGGING_INTERVAL = "customprecisiontime";
    /**
     * Preference key for logging custom precision distance
     */
    public static final String LOGGING_DISTANCE = "customprecisiondistance";

    /**
     * Preference key for TODO
     */
    public static final String SPEEDSANITYCHECK = "speedsanitycheck";

    /**
     * Preference key for TODO
     */
    public static final String STATUS_MONITOR = "gpsstatusmonitor";

    public static final String LOGATSTARTUP = "logatstartup";

    public static final String DATASOURCES_KEY = "DATASOURCES";
    public static final Class<?> LOGGER_ACTIVITY_CLASS = TrackMapActivity.class;
}
