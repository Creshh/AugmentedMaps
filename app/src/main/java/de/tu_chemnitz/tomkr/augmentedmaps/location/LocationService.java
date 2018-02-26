package de.tu_chemnitz.tomkr.augmentedmaps.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.LOCATION_UPDATE_DISTANCE;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.LOCATION_UPDATE_INTERVAL;


/**
 * Created by Tom Kretzschmar on 01.10.2017.<br>
 * <br>
 * Service which provides user device location changes.<br>
 * Uses Network and GPS providers.
 *
 * Service can be improved further, using proper location handling with usage of both providers and a current best estimate.
 */
public class LocationService {
    /**
     * Tag for logging
     */
    private static final String TAG = LocationService.class.getName();

    /**
     * The location manager system service to access the different location providers.
     */
    private LocationManager locationManager;

    /**
     * An array of location listeners for the different location providers.
     */
    private LocationListener[] mLocationListeners;

    /**
     * Last (known) location of the user device. Will be the one returned to the listeners.
     */
    private Location lastLocation;

    /**
     * A List of {@link LocationListener} which are notified if location changes.
     */
    private List<de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener> listeners = new ArrayList<>();

    /**
     * Register a {@link LocationListener} to get notified when the location of the user device changes.
     * @param listener The listener instance to be registered.
     */
    public void registerListener(de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Remove a {@link LocationListener} from the Service.
     * @param listener The listener instance to be unregistered.
     */
    public void unregisterListener(de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener listener) {
        this.listeners.remove(listener);
    }

    /**
     * Conveniance method to get the current last known location pushed to all listeners, even if there wasn't a real location update in the past time.
     */
    public void pushLocation() {
        Log.d(TAG, "pushLocation");
        for (de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener listener : listeners) {
            listener.onLocationChange(new de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location((float) lastLocation.getLatitude(), (float) lastLocation.getLongitude(), (int) lastLocation.getAltitude()));
        }
    }

    /**
     * Full constructor.
     * @param context Application or activity context to initialize the service.
     */
    public LocationService(Context context) {
        Log.d(TAG, "initializeLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        mLocationListeners = new LocationListener[]{new LocationListener(LocationManager.GPS_PROVIDER), new LocationListener(LocationManager.NETWORK_PROVIDER)};
    }

    /**
     * Start service listening for location changes.
     */
    public void start() {
        Log.d(TAG, "startGPS");
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISTANCE, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_UPDATE_INTERVAL, LOCATION_UPDATE_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    /**
     * Stop service listening for location changes. Should be called when application gets paused or no location information are needed anymore to save battery and computational resources.
     */
    public void stop() {
        Log.d(TAG, "stopGPS");
        if (locationManager != null) {
            for (LocationListener listener : mLocationListeners) {
                try {
                    locationManager.removeUpdates(listener);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    /**
     * A private inner class implementing a LocationListener for the system location providers.
     */
    private class LocationListener implements android.location.LocationListener {

        /**
         * Full constructor.
         * @param provider The provider this listener is targeted to.
         */
        private LocationListener(String provider) {
            lastLocation = new Location(provider);
            Log.d(TAG, "LocationListener " + provider + " Location: " + lastLocation);
            try {
                Log.d(TAG, "LocationListener " + provider + " LastKnownLocation: " + locationManager.getLastKnownLocation(provider));
                if (locationManager.getLastKnownLocation(provider) != null) lastLocation = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException ex) {
                Log.e(TAG, ex.getMessage());
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + location);
            lastLocation.set(location);
            for (de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener listener : listeners) {
                listener.onLocationChange(new de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location((float) location.getLatitude(), (float) location.getLongitude(), (int) location.getAltitude()));
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(TAG, "onStatusChanged: " + provider);
        }
    }
}
