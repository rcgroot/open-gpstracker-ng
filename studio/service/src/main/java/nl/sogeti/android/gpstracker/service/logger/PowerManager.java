package nl.sogeti.android.gpstracker.service.logger;

import android.content.Context;

import nl.sogeti.android.gpstracker.service.integration.ServiceConstants;

public class PowerManager {

    private static final String TAG = "WakeLockTag";
    private final Context mContext;
    private android.os.PowerManager.WakeLock mWakeLock;

    PowerManager(Context context) {
        this.mContext = context;
    }

    public void updateWakeLock(int loggingState) {
        if (loggingState == ServiceConstants.STATE_LOGGING) {
            android.os.PowerManager pm = (android.os.PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            if (this.mWakeLock != null) {
                this.mWakeLock.release();
                this.mWakeLock = null;
            }
            this.mWakeLock = pm.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, TAG);
            this.mWakeLock.acquire();
        } else {
            if (this.mWakeLock != null) {
                this.mWakeLock.release();
                this.mWakeLock = null;
            }
        }
    }

    public void release() {
        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if (mWakeLock != null) {
            mWakeLock.release();
        }
    }
}
