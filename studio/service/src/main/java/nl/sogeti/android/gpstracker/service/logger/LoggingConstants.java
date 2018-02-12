package nl.sogeti.android.gpstracker.service.logger;

/**
 * Created by grootren on 13-11-15.
 */
public class LoggingConstants {
    public static final long GLOBAL_INTERVAL = 300000l;
    public static final float FINE_DISTANCE = 5F;
    public static final long FINE_INTERVAL = 1000l;
    public static final float FINE_ACCURACY = 20f;
    public static final float NORMAL_DISTANCE = 10F;
    public static final long NORMAL_INTERVAL = 15000l;
    public static final float NORMAL_ACCURACY = 30f;
    public static final float COARSE_DISTANCE = 25F;
    public static final long COARSE_INTERVAL = 30000l;
    public static final float COARSE_ACCURACY = 75f;
    public static final float GLOBAL_DISTANCE = 500F;
    public static final float GLOBAL_ACCURACY = 1000f;
    public static final String SERVICESTATE_DISTANCE = "SERVICESTATE_DISTANCE";
    public static final String SERVICESTATE_STATE = "SERVICESTATE_STATE";
    public static final String SERVICESTATE_PRECISION = "SERVICESTATE_PRECISION";
    public static final String SERVICESTATE_SEGMENTID = "SERVICESTATE_SEGMENTID";
    public static final String SERVICESTATE_TRACKID = "SERVICESTATE_TRACKID";
}
