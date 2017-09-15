package de.tu_chemnitz.tomkr.augmentedmaps.util;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by Tom Kretzschmar on 15.09.2017.
 *
 */

public class Helpers {

    /**
     * Shows a {@link Toast} on the UI thread.
     *
     * @param text The message to show
     */
    public static void showToast(final String text, final Activity activity) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

}
