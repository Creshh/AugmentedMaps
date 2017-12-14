package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.opencv.core.Point;

import java.util.Iterator;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.Constants;
import de.tu_chemnitz.tomkr.augmentedmaps.core.Controller;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class ARView extends View {
    private static final String TAG = ARView.class.getName();

    private List<Marker> markerDrawables;
    private Point[] points;
    private Vec2f debugVec;

    public ARView(Context context) {
        super(context);
        init(null, 0);
    }

    public ARView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ARView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        Log.d(TAG, "onDraw");



        int width = getWidth();
        int height = getHeight();
//        Log.d(TAG, "Size: " + width + "___" + height);

        canvas.drawCircle(960, 540, 20, Constants.paintStroke);

        if(markerDrawables != null) {
            synchronized (Controller.listLock) {
                for (Marker md : markerDrawables) {
//            Log.d(TAG, "Draw Marker " + md);
                    md.setSize(width, height);
                    md.draw(canvas);
                }
            }
        }

        if (points != null ){
            for( Point p : points){
                canvas.drawCircle((float)p.x, (float)(p.y), 10, Constants.paintStroke);
//                canvas.drawCircle((float)p.x, (float)(1080-p.y), 10, Constants.paintStroke);
//                canvas.drawCircle((float)(p.y), (float)(1080-p.x), 10, Constants.paintStroke);
//                canvas.drawCircle(1920-(float)p.x, 1080-(float)p.y, 10, Constants.paintFill); // TODO: correct coordinate system orientation
//                canvas.drawCircle((float)p.x, (float)p.y, 10, Constants.paintStroke);
            }
        }
        if(debugVec != null){
            canvas.drawLine(width/2, height/2, (width/2) + debugVec.getX(), (height/2) + debugVec.getY(), Constants.paintStroke);
        }
    }

    public void setMarkerListRef(List<Marker> markerDrawables){
        this.markerDrawables = markerDrawables;
    }

//    public void setFeaturesToDraw(List<FeatureDrawable> featuresToDraw){
//        // TODO: debug drawing of Features for OpenCV FeatureDetection
//    }

    public void setDebugArray(Point[] points){
        this.points = points;
    }

    public void setDebugVec(Vec2f debugVec) {
        this.debugVec = debugVec;

    }
}
