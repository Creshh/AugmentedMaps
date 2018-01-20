package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;


/**
 * Created by Tom Kretzschmar on 06.10.2017.<br>
 * <br>
 * An {@link ElevationService} which uses the open elevation api to query altitude information for given location objects.<br>
 * See: https://open-elevation.com/
 */
public class OpenElevationService implements ElevationService {
    /**
     * Tag for logging
     */
    private static final String TAG = OpenElevationService.class.getName();

    /**
     * Target url for open elevation api
     */
    private static final String OPEN_ELEVATION = "https://api.open-elevation.com/api/v1/lookup";

    /**
     * Acquires and sets the altitude to the given locations using the open-elevation api.
     * @param locs The geolocations whithout altitude information.
     * @return The geolocations given with acquired altitude information.
     */
    @Override
    public Location[] getElevation(Location[] locs) {

        StringBuilder query = new StringBuilder("locations=");
        for (int i = 0; i < locs.length; i++) {
            if (i != 0) query.append("|");
            query.append(locs[i].getLat()).append(",").append(locs[i].getLon());
        }
        Log.d(TAG, "Query:" + query.toString());


        URL url = null;
        try {
            url = new URL(OPEN_ELEVATION + "?" + query.toString());
            Log.d(TAG, "URL:" + url.toString());
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
        }
        try {
            JsonReader reader = new JsonReader(new InputStreamReader(response, "utf-8"));
            reader.beginObject();
            reader.skipValue();
            reader.beginArray();
            int i = 0;
            while (reader.hasNext()) {
                reader.beginObject();
                Log.d(TAG, "---------------------------------");
                while (reader.hasNext()) {
                    String name = reader.nextName();
                    String log = name;
                    if (name.equals("elevation")) {
                        locs[i].setAlt(reader.nextInt());
                        log += " -> " + locs[i].getAlt();
                    } else {
                        reader.skipValue();
                        log += " -> skipped";
                    }
                    Log.d(TAG, log);
                }
                reader.endObject();
                i++;
                Log.d(TAG, "---------------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
