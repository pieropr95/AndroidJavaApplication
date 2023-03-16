package test.pprivera.com.testapplicationtraccar3;

import android.content.Context;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

@SuppressWarnings("MissingPermission")
public class MixedPositionProvider extends PositionProvider implements LocationListener, GpsStatus.Listener {

    private static final int FIX_TIMEOUT = 30 * 1000;

    private LocationListener backupListener;
    private long lastFixTime;

    public MixedPositionProvider(Context context, PositionListener listener) {
        super(context, listener);
    }

    public void startUpdates() {
        lastFixTime = System.currentTimeMillis();
        locationManager.addGpsStatusListener(this);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, requestInterval, 0, this);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, e);
        }
    }

    public void stopUpdates() {
        locationManager.removeUpdates(this);
        locationManager.removeGpsStatusListener(this);
        stopBackupProvider();
    }

    private void startBackupProvider() {
        Log.i(TAG, "backup provider start");
        if (backupListener == null) {

            backupListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.i(TAG, "backup provider location");
                    updateLocation(location);
                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {
                }

                @Override
                public void onProviderEnabled(String s) {
                }

                @Override
                public void onProviderDisabled(String s) {
                }
            };

            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, requestInterval, 0, backupListener);
        }
    }

    private void stopBackupProvider() {
        Log.i(TAG, "backup provider stop");
        if (backupListener != null) {
            locationManager.removeUpdates(backupListener);
            backupListener = null;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "provider location");
        stopBackupProvider();
        lastFixTime = System.currentTimeMillis();
        updateLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i(TAG, "provider enabled");
        stopBackupProvider();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i(TAG, "provider disabled");
        startBackupProvider();
    }

    @Override
    public void onGpsStatusChanged(int event) {
        if (backupListener == null && System.currentTimeMillis() - lastFixTime - requestInterval > FIX_TIMEOUT) {
            startBackupProvider();
        }
    }

}
