package de.tu_chemnitz.tomkr.augmentedmaps.util;

import android.app.Activity;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Tom Kretzschmar on 15.09.2017.
 *
 */

public class Helpers {


    public static String createTimeStamp(long timestamp) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(timestamp);
        Date d = c.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.GERMANY);
        return sdf.format(d);
    }

}
