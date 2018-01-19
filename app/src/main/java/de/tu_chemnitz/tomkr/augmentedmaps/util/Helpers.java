package de.tu_chemnitz.tomkr.augmentedmaps.util;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
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

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.TimedVec2f;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.CONFIG_FILE_RESOURCE;

/**
 * Created by Tom Kretzschmar on 15.12.2017.<br>
 * <br>
 * A class implementing globally available helper functions.
 */

public class Helpers {
    /**
     * Tag for logging
     */
    private static final String TAG = Helpers.class.getName();

    /**
     * Get named value from given configFile. See {@link de.tu_chemnitz.tomkr.augmentedmaps.core.Const#CONFIG_FILE_RESOURCE}.
     * @param context Application or Activity context.
     * @param name Name of the value to read.
     * @return The value which was read.
     */
    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(CONFIG_FILE_RESOURCE);
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

    /**
     * Read OSM tags from config file.
     * @param context Application or Activity context.
     * @return A Map of the OSM tags.
     */
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

    /**
     * Crate a human readable String representation of a timestamp.
     * @param timestamp The timestamp to format.
     * @return The timestamp in the pattern yyyyMMdd_HHmm
     */
    public static String createTimeStamp(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.GERMANY);
        return sdf.format(d);
    }

    /**
     * Save a List of {@link TimedVec2f} to a file.
     * @param dataLog The List to write.
     * @param suffix A String suffix to append to the file name.
     * @return The path to the written file.
     */
    public static String saveLogToFile(ArrayList<TimedVec2f> dataLog, String suffix) {
        String filename = "sensor_" + createTimeStamp(System.currentTimeMillis()) + "_" + suffix + ".log";
        StringBuilder builder = new StringBuilder();
        for (TimedVec2f p : dataLog) {
            builder.append(p.getTimestamp());
            builder.append(" ");
            builder.append(p.getX());
            builder.append(" ");
            builder.append(p.getY());
            builder.append(System.lineSeparator());
        }
        File fileDir = new File(Environment.getExternalStorageDirectory(), "AugmentedMapsLog");
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        FileOutputStream outputStream;
        File file = new File(fileDir, filename);
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(builder.toString().getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "File " + file.getAbsolutePath() + " written!");
        return file.getAbsolutePath();
    }
}
