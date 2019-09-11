package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import de.tu_chemnitz.tomkr.augmentedmaps.core.Const;


/**
 * Created by Tom Kretzschmar on 14.11.2017.<br>
 * <br>
 * Data and view class of a Marker representing a MapNode mapped to the augmented reality level.<br>
 * Contains both data and draw function to minimize overhead of two separate implementations.
 */
public class Marker extends Drawable {
    /**
     * Tag for logging
     */
    private static final String TAG = Marker.class.getName();

    /**
     * Width in pixel of drawn representation.
     */
    private static final int WIDTH = 30;

    /**
     * Height in pixel of drawn representation.
     */
    private static final int HEIGHT = 30;

    /**
     * The width of the targeted display/view.
     */
    private int displayWidth;

    /**
     * The height of the targeted display/view.
     */
    private int displayHeight;

    /**
     * The relative position of the marker on the x axis. Contains values in [0..1]
     */
    private float x;

    /**
     * The relative position of the marker on the y axis. Contains values in [0..1]
     */
    private float y;

    /**
     * Key or name of the Marker, which will be displayed next to the exact position.
     */
    private String key;

    /**
     * Full constructor.
     * @param x The relative position of the marker on the x axis. Contains values in [0..1]
     * @param y The relative position of the marker on the y axis. Contains values in [0..1]
     * @param key Key or name of the Marker, which will be displayed next to the exact position.
     */
    public Marker(float x, float y, String key) {
        this.x = x;
        this.y = y;
        this.key = key;

    }

    /**
     * Draw the marker on the given canvas.
     * @param canvas the canvas to draw on
     */
    @Override
    public void draw(@NonNull Canvas canvas) {
//        Log.d(TAG, "draw " + key + " " + x + " " + y);

        if(x > 0) {
            //recalculate from Marker x/y [0..1] to pixel values [0..1920] etc.
            int xPx = (int) (x * displayWidth);
            int yPx = (int) (y * displayHeight);

//            Log.d(TAG, "draw " + key + " " + x + " " + y);
            canvas.drawRect(xPx - (WIDTH / 2), yPx - (HEIGHT / 2), xPx + (WIDTH / 2), yPx + (HEIGHT / 2), Const.paintStroke);
            canvas.drawRect(xPx - (WIDTH / 4), yPx - (HEIGHT / 4), xPx + (WIDTH / 4), yPx + (HEIGHT / 4), Const.paintFill);
            if(key != null){
                canvas.save();
                canvas.rotate(-90f, xPx + (WIDTH/2), yPx - HEIGHT);
                canvas.drawText(key, xPx + (WIDTH/2), yPx - HEIGHT, Const.paintFill);
                canvas.restore();
            }
        }
    }

    /**
     * Not implemented!
     */
    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int a) {}

    /**
     * Set colorFilter to paint objects.
     * @param colorFilter The color filter to apply.
     */
    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        Const.paintFill.setColorFilter(colorFilter);
        Const.paintStroke.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    /**
     * @return A String representation of the Marker in the format Marker{key|x|y}
     */
    @Override
    public String toString() {
        return "Marker{" + key + "|" + x + "|" + y + "}";
    }

    /**
     * Set the targeted display or view size. Must be set to draw at the correct position.
     * @param width The width of the display in pixel.
     * @param height The height of the display in pixel.
     */
    public void setDisplaySize(int width, int height) {
        this.displayWidth = width;
        this.displayHeight = height;
    }
}
