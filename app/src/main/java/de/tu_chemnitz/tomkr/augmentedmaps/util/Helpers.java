package de.tu_chemnitz.tomkr.augmentedmaps.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Marker;

/**
 * Created by Tom Kretzschmar on 15.09.2017.
 *
 */

public class Helpers {
    private static final String TAG = Helpers.class.getName();

    private static Random random;

    /**
     * Initialisiert statische Variablen.
     */
    static{
        random = new Random();
    }

    public static String createTimeStamp(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY);
        return sdf.format(d);
    }


//    public static List<Marker> createSampleMarker(int count, int maxWidth, int maxHeight){
//        List<Marker> list = new ArrayList<>();
//        for(int i = 0; i < count; i++){
//            Marker m = new Marker(random(maxWidth), random(maxHeight), "randM");
//            Marker md = new Marker(m);
//            Log.d(TAG, "createMarker " + md);
//            list.add(md);
//        }
//        return list;
//    }

    /**
     * Ermittelt Zufallswert im Intervall um die Basis von base-variance bis base+variance.
     * @param base Basis
     * @param variance Intervall um die Basis
     * @return Zufallszahl
     */
    public static float randomInterval(float base, float variance){
        return randomInRange(base-variance, base+variance);
    }

    /**
     * Ermittelt Zufallswert zwischen low und high.
     * @param low Unterer Grenzwert
     * @param high Oberer Grenzwert
     * @return Zufallszahl
     */
    public static float randomInRange(float low, float high){
        return random() * (high-low) + low;
    }

    /**
     * Ermittelt Zufallswert zwischen 0 und high.
     * @param high Oberer Grenzwert
     * @return Zufallszahl
     */
    public static float random(float high){
        return random() * high;
    }

    /**
     * Ermittelt Zufallswert zwischen 0 und 1
     * @return Zufallszahl
     */
    public static float random(){
        return random.nextFloat();
    }

    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open config file.");
        }

        return null;
    }

    public static Map<String, List<String>> getTagsFromConfig(Context context) {
        Map<String, List<String>> tags = new HashMap<>();
        String configValue = Helpers.getConfigValue(context, "tags");
        Log.d(TAG, "configValue: " + configValue);
        String[] sets = configValue.split("\\|");
        for (String set : sets) {
            Log.d(TAG, "set: " + set);
            String key = set.split(":")[0];
            String[] values = set.split(":")[1].split(",");
            tags.put(key, new ArrayList<String>());
            for (String value : values) {
                tags.get(key).add(value);
            }
        }
        return tags;
    }
}
