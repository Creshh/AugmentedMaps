package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import android.annotation.SuppressLint;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;

/**
 * Created by Tom Kretzschmar on 06.10.2017.<br>
 * <br>
 * An {@link ElevationService} which uses the racemap elevation api to query altitude information for given location objects.<br>
 * See: https://github.com/racemap/elevation-service
 */
@SuppressLint("NewApi")
public class RaceMapElevationService implements ElevationService {
    /**
     * Tag for logging
     */
    private static final String TAG = RaceMapElevationService.class.getName();

    /**
     * Target url for open elevation api
     */
    private static final String RACEMAP_COM_API = "https://elevation.racemap.com/api";

    /**
     * Acquires and sets the altitude to the given locations using the open-elevation api.
     * @param locs The geolocations whithout altitude information.
     * @return The geolocations given with acquired altitude information.
     */
    @Override
    public Location[] getElevation(Location[] locs) {

        // ?lat=42.7lng=2.8
        StringBuilder query = new StringBuilder("lat=");
        for (int i = 0; i < locs.length; i++) {
            if (i != 0) query.append("|");
            query.append(locs[i].getLat()).append("&lng=").append(locs[i].getLon());
        }
        Log.w(TAG, "Query:" + query.toString());

        URL url = null;
        try {
            url = new URL(RACEMAP_COM_API + "?" + query.toString());
            Log.w(TAG, "URL:" + url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setRequestProperty("Accept-Charset", "utf-8");
        InputStream response = null;
        try {
            response = connection.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return locs;
        }
            String elevation = new BufferedReader(new InputStreamReader(response, StandardCharsets.UTF_8)).
                              lines().reduce(String::concat).get();
            Log.w(TAG, "---------------------------------");
            locs[0].setAlt(Double.valueOf(elevation).intValue());
            Log.w(TAG, " -> " + locs[0].getAlt());
            Log.w(TAG, "---------------------------------");

        return locs;
    }

    /**
     * Acquires and sets the altitude to the given location using the open-elevation api.
     * @param loc The geolocation whithout altitude information.
     * @return The geolocation given with acquired altitude information.
     */
    @Override
    public Location getElevation(Location loc) {
        return getElevation(new Location[]{loc})[0];
    }

}
