package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;

import static android.R.attr.x;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class MarkerDrawable extends Drawable {
    private static final String TAG = "H_POS";
    private Marker marker;
    private Paint paintStroke;
    private Paint paintFill;

    private static final int WIDTH = 30;
    private static final int HEIGHT = 30;
    private int w;
    private int h;

    public MarkerDrawable(Marker marker) {



        this.marker = marker;
        this.paintStroke = new Paint();
        this.paintStroke.setColor(Color.GREEN);
        this.paintStroke.setStyle(Paint.Style.STROKE);
        this.paintStroke.setStrokeWidth(3);
        this.paintStroke.setAntiAlias(true);
        this.paintStroke.setAlpha(255);

        this.paintFill = new Paint();
        this.paintFill.setColor(Color.GREEN);
        this.paintFill.setStyle(Paint.Style.FILL);
        this.paintFill.setAntiAlias(true);
        this.paintFill.setAlpha(255);
        this.paintFill.setTextAlign(Paint.Align.LEFT);
        this.paintFill.setTextSize(40);
        this.paintFill.setAntiAlias(true);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if(marker.getX() > 0) {
            int x = (int) (marker.getX() * w);// TODO -> recalculate from Marker x/y [0..1] to pixel values [0..1080] etc.
            int y = (int) marker.getY();

//            Log.d(TAG, "w=" + w + " x=" + x);

            canvas.drawRect(x - (WIDTH / 2), y - (HEIGHT / 2), x + (WIDTH / 2), y + (HEIGHT / 2), paintStroke);
            canvas.drawRect(x - (WIDTH / 4), y - (HEIGHT / 4), x + (WIDTH / 4), y + (HEIGHT / 4), paintFill);
            if(marker.getKey() != null){
                canvas.save();
                canvas.rotate(-90f, x + (WIDTH/2), y-HEIGHT);
                canvas.drawText(marker.getKey(), x + (WIDTH/2), y - HEIGHT, paintFill);
                canvas.restore();
            }
        }
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int a) {
        this.paintStroke.setAlpha(a);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        this.paintStroke.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public String toString() {
        return "MD[" + marker.getX() + "|" + marker.getY() + "]";
    }

    public void setSize(int width, int height) {
        this.w = width;
        this.h = height;
    }
}
