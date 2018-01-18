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

import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.TimedVec2f;

/**
 * Created by Tom Kretzschmar on 15.09.2017.
 */

public class Helpers {
    private static final String TAG = Helpers.class.getName();

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

    public static String createTimeStamp(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.GERMANY);
        return sdf.format(d);
    }

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
