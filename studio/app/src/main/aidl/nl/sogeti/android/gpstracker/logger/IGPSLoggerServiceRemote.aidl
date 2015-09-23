package nl.sogeti.android.gpstracker.logger;

import android.net.Uri;
import android.location.Location;

interface IGPSLoggerServiceRemote {

	int loggingState();
    long startLogging();
    void pauseLogging();
    long resumeLogging();
	void stopLogging();
	Uri storeMediaUri(in Uri mediaUri);
    boolean isMediaPrepared();
    void storeDerivedDataSource(in String sourceName);
    Location getLastWaypoint();
    float getTrackedDistance();
}