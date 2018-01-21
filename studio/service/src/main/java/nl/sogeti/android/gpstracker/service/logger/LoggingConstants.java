package nl.sogeti.android.gpstracker.service.logger;

/**
 * Created by grootren on 13-11-15.
 */
public class LoggingConstants {
    static final long GLOBAL_INTERVAL = 300000l;
    static final float FINE_DISTANCE = 5F;
    static final long FINE_INTERVAL = 1000l;
    static final float FINE_ACCURACY = 20f;
    static final float NORMAL_DISTANCE = 10F;
    static final long NORMAL_INTERVAL = 15000l;
    static final float NORMAL_ACCURACY = 30f;
    static final float COARSE_DISTANCE = 25F;
    static final long COARSE_INTERVAL = 30000l;
    static final float COARSE_ACCURACY = 75f;
    static final float GLOBAL_DISTANCE = 500F;
    static final float GLOBAL_ACCURACY = 1000f;
    static final String SERVICESTATE_DISTANCE = "SERVICESTATE_DISTANCE";
    static final String SERVICESTATE_STATE = "SERVICESTATE_STATE";
    static final String SERVICESTATE_PRECISION = "SERVICESTATE_PRECISION";
    static final String SERVICESTATE_SEGMENTID = "SERVICESTATE_SEGMENTID";
    static final String SERVICESTATE_TRACKID = "SERVICESTATE_TRACKID";
}
