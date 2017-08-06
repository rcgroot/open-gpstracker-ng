package nl.sogeti.android.gpstracker.integration;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface ServiceManagerInterface {

    void startup(@NonNull Context context, @Nullable Runnable runnable);

    void shutdown(@NonNull Context context);

    int getLoggingState();

    long getTrackId();

    void startGPSLogging(@NonNull Context context, @Nullable String trackName);

    void stopGPSLogging(@NonNull Context context);

    void pauseGPSLogging(@NonNull Context context);

    void resumeGPSLogging(@NonNull Context context);

    boolean isPackageInstalled(Context context);
}
