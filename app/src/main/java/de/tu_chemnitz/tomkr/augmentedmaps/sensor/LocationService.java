package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Tom Kretzschmar on 01.09.2017.
 *
 */

public class LocationService {

    private static final String TAG = LocationService.class.getName();

    private LocationManager locationManager;
    private LocationListener[] mLocationListeners;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final int DIST = 500;

    private Location lastBigLocation;
    private Location lastSmallLocation;
    private List<de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener> listeners = new ArrayList<>();

    public void registerListener(de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener listener){
        this.listeners.add(listener);
    }

    public void unregisterListener(de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener listener){
        this.listeners.remove(listener);
    }

    public void pushLocation(){
        for(de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener listener : listeners){
            listener.onInitialLocation(new de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location((float)lastBigLocation.getLatitude(), (float)lastBigLocation.getLongitude(), (float)lastBigLocation.getAltitude()));
        }
    }

    public LocationService(Context context) {
        Log.e(TAG, "initializeLocationManager");
        if (locationManager == null) {
            locationManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        mLocationListeners = new LocationListener[] {
                new LocationListener(LocationManager.GPS_PROVIDER),
                new LocationListener(LocationManager.NETWORK_PROVIDER)
        };
    }

    public void start() {
        Log.e(TAG, "startGPS");
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    public void stop() {
        Log.e(TAG, "stopGPS");
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
            lastSmallLocation = new Location(provider);
            Log.e(TAG, "LocationListener " + provider + " Location: " + lastSmallLocation);
            try {
                Log.e(TAG, "LocationListener " + provider + " LastKnownLocation: " + locationManager.getLastKnownLocation(provider));
                if(locationManager.getLastKnownLocation(provider) != null)
                    lastBigLocation = locationManager.getLastKnownLocation(provider);
                    lastSmallLocation = locationManager.getLastKnownLocation(provider);
            } catch (SecurityException ex) {

            }
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged: " + location);
            lastSmallLocation.set(location);
            if(lastBigLocation.distanceTo(location)>DIST){
                lastBigLocation = location;
                for(de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener listener : listeners){
                    listener.onBigLocationChange(new de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location((float)location.getLatitude(), (float)location.getLongitude(), (float)location.getAltitude()));
                }
            }
            for(de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener listener : listeners){
                listener.onSmallLocationChange(new de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location((float)location.getLatitude(), (float)location.getLongitude(), (float)location.getAltitude()));
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }
}
