package nl.sogeti.android.gpstracker.service.integration;

import androidx.annotation.Nullable;

public interface ServiceManagerInterface {

    void startup(@Nullable Runnable runnable);

    void shutdown();

    int getLoggingState();

    long getTrackId();

    void startGPSLogging(@Nullable String trackName);

    void stopGPSLogging();

    void pauseGPSLogging();

    void resumeGPSLogging();

    boolean isPackageInstalled();
}
