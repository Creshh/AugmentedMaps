package de.tu_chemnitz.tomkr.augmentedmaps.core.types;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import de.tu_chemnitz.tomkr.augmentedmaps.core.Constants;


/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */
public class Marker extends Drawable {
    private static final String TAG = Marker.class.getName();

    private static final int WIDTH = 30;
    private static final int HEIGHT = 30;
    private int w;
    private int h;
    private float x;
    private float y;
    private String key;

    public Marker(float x, float y, String key) {
        this.x = x;
        this.y = y;
        this.key = key;

    }

    @Override
    public void draw(@NonNull Canvas canvas) {
//        Log.d(TAG, "draw " + key + " " + x + " " + y);

        if(x > 0) {
            int xPx = (int) (x * w); //recalculate from Marker x/y [0..1] to pixel values [0..1080] etc.
            int yPx = (int) (y * h);

//            Log.d(TAG, "draw " + key + " " + x + " " + y);
            canvas.drawRect(xPx - (WIDTH / 2), yPx - (HEIGHT / 2), xPx + (WIDTH / 2), yPx + (HEIGHT / 2), Constants.paintStroke);
            canvas.drawRect(xPx - (WIDTH / 4), yPx - (HEIGHT / 4), xPx + (WIDTH / 4), yPx + (HEIGHT / 4), Constants.paintFill);
            if(key != null){
                canvas.save();
                canvas.rotate(-90f, xPx + (WIDTH/2), yPx - HEIGHT);
                canvas.drawText(key, xPx + (WIDTH/2), yPx - HEIGHT, Constants.paintFill);
                canvas.restore();
            }
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int a) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        Constants.paintFill.setColorFilter(colorFilter);
        Constants.paintStroke.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public String toString() {
        return "Marker[" + key + "|" + x + "|" + y + "]";
    }

    public void setSize(int width, int height) {
        this.w = width;
        this.h = height;
    }
}
