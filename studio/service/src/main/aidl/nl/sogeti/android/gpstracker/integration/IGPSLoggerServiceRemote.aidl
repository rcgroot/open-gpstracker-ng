package nl.sogeti.android.gpstracker.integration;

import android.net.Uri;
import android.location.Location;

interface IGPSLoggerServiceRemote {

    long getTrackId();
	int loggingState();
    boolean isMediaPrepared();
	Uri storeMediaUri(in Uri mediaUri);
    Uri storeMetaData(in String key, in String value);
    Location getLastWaypoint();
    float getTrackedDistance();
}