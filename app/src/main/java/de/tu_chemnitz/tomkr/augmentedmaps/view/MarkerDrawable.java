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

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class MarkerDrawable extends Drawable{

    private Marker marker;
    private Paint paintStroke;
    private Paint paintFill;

    private static final int WIDTH = 30;
    private static final int HEIGHT = 30;

    public MarkerDrawable(Marker marker){
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
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRect(marker.getX()-(WIDTH/2), marker.getY()-(HEIGHT/2), marker.getX() + (WIDTH/2), marker.getY() + (HEIGHT/2), paintStroke);
        canvas.drawRect(marker.getX()-(WIDTH/4), marker.getY()-(HEIGHT/4), marker.getX() + (WIDTH/4), marker.getY() + (HEIGHT/4), paintFill);
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
    public String toString(){
        return "MD[" + marker.getX() + "|" + marker.getY() + "]";
    }
}
