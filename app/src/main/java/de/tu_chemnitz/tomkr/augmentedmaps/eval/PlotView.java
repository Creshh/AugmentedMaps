package de.tu_chemnitz.tomkr.augmentedmaps.eval;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;

import static android.R.attr.centerX;
import static android.R.attr.centerY;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.paintStroke;

/**
 * Created by Tom Kretzschmar on 14.11.2017.
 *
 */

public class PlotView extends View implements OrientationListener {

    private ArrayList<OrientationDrawable> orientations;

    private int cXx;
    private int cXy;
    private int cYx;
    private int cYy;

    public PlotView(Context context) {
        super(context);
        orientations = new ArrayList<>();
        setBackgroundColor(Color.BLACK);
    }

    public PlotView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        orientations = new ArrayList<>();
        setBackgroundColor(Color.BLACK);
    }

    public PlotView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        orientations = new ArrayList<>();
        setBackgroundColor(Color.BLACK);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cXx = 50;
        cXy = h/4;
        cYx = w/4;
        cYy = h/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (orientations){
            for (Iterator<OrientationDrawable> iterator = orientations.iterator(); iterator.hasNext();) {
                OrientationDrawable o = iterator.next();
                if(o.alive) {
                    o.draw(canvas);
                } else {
                    iterator.remove();
                }
            }
        }

//        canvas.drawLine(cXx - 100, cXy, centerX + 100, cx, paintStroke);
//        canvas.drawLine(centerX, centerY -100 , centerX, centerY + 100, paintStroke);
    }

    @Override
    public void onOrientationChange(Orientation values) {
        synchronized (orientations) {
            orientations.add(new OrientationDrawable(values));
//            Log.d("PlotView", "Points: " + orientations.size());
        }
        invalidate();
    }


    private class OrientationDrawable extends Drawable{

        private final Orientation orientation;
        private long starttime;
        private long lifeTime = 0;
        private long ttl = 3000;



        boolean alive = true;
        private Paint paint;

        public OrientationDrawable(Orientation orientation){
            starttime = System.currentTimeMillis();
            this.orientation = orientation;
            paint = new Paint();
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.FILL);
            paint.setAntiAlias(true);
            paint.setAlpha(20);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setAntiAlias(true);


        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            lifeTime = System.currentTimeMillis() - starttime;
            if(lifeTime >= ttl){
                alive = false;
            } else {
                paint.setAlpha((int) (20 * (1 - (lifeTime / ttl))));
//                canvas.drawCircle(centerX + orientation.getX(), centerY + orientation.getY(), 15, paint);
                canvas.drawCircle(cXx + (orientation.getX()*6), cXy, 25, paint);
                canvas.drawCircle(cYx, cYy + (-3*(orientation.getY() > 180 ? 360 - orientation.getY() : - orientation.getY())), 25, paint);
            }
        }

        @Override
        public void setAlpha(@IntRange(from = 0, to = 255) int i) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

}



