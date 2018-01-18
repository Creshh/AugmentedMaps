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
 * Created by Tom Kretzschmar on 06.10.2017.
 *
 */

public class OpenElevationService implements ElevationService {

    private static final String TAG = OpenElevationService.class.getName();
    public static final String OPEN_ELEVATION = "https://api.open-elevation.com/api/v1/lookup";

    @Override
    public Location[] getElevation(Location[] locs) {

        String query = "locations=";
        for (int i = 0; i < locs.length; i++) {
            if (i != 0) query += "|";
            query += locs[i].getLat() + "," + locs[i].getLon();
        }
        Log.d(TAG, "Query:" + query);


        URL url = null;
        try {
            url = new URL(OPEN_ELEVATION + "?" + query);
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
                        locs[i].setHeight(reader.nextInt());
                        log += " -> " + locs[i].getHeight();
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

    @Override
    public Location getElevation(Location loc) {
        return getElevation(new Location[]{loc})[0];
    }

}
