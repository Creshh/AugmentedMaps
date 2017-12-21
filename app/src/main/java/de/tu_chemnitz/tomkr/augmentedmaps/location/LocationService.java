package de.tu_chemnitz.tomkr.augmentedmaps.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.LOCATION_UPDATE_DISTANCE;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.LOCATION_UPDATE_INTERVAL;


/**
 * Created by Tom Kretzschmar on 01.09.2017.
 *
 */

public class LocationService {

    private static final String TAG = LocationService.class.getName();

    private LocationManager locationManager;
    private LocationListener[] mLocationListeners;


    private Location currentBestEstimate;

    private Location lastLocation;
    private List<de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener> listeners = new ArrayList<>();

    public void registerListener(de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener listener){
        this.listeners.add(listener);
    }

    public void unregisterListener(de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener listener){
        this.listeners.remove(listener);
    }

    public void pushLocation(){
        Log.d(TAG, "pushLocation");
        for(de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener listener : listeners){
            listener.onLocationChange(new de.tu_chemnitz.tomkr.augmentedmaps.core.types.Location((float)lastLocation.getLatitude(), (float)lastLocation.getLongitude(), (int)lastLocation.getAltitude()));
        }
    }

    public LocationService(Context context) {
        Log.d(TAG, "initializeLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        mLocationListeners = new LocationListener[] {
                new LocationListener(LocationManager.GPS_PROVIDER),
                new LocationListener(LocationManager.NETWORK_PROVIDER)
        };
    }

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

    public void stop() {
        Log.d(TAG, "stopGPS");
        if (locationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    locationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private class LocationListener implements android.location.LocationListener {


        public LocationListener(String provider) {
            lastLocation = new Location(provider);
            Log.d(TAG, "LocationListener " + provider + " Location: " + lastLocation);
            try {
                Log.d(TAG, "LocationListener " + provider + " LastKnownLocation: " + locationManager.getLastKnownLocation(provider));
                if(locationManager.getLastKnownLocation(provider) != null)
                    lastLocation = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException ex) {

            }
        }

        // TODO proper Location handling with usage of both providers
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged: " + location);
            lastLocation.set(location);
            for(de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener listener : listeners){
                listener.onLocationChange(new de.tu_chemnitz.tomkr.augmentedmaps.core.types.Location((float)location.getLatitude(), (float)location.getLongitude(), (int)location.getAltitude()));
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
