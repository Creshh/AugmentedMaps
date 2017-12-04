package de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing;

import android.graphics.Bitmap;
import android.media.Image;
import android.util.Log;

import org.opencv.core.Point;

import de.tu_chemnitz.tomkr.augmentedmaps.core.OpenCVHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;
import de.tu_chemnitz.tomkr.augmentedmaps.view.ARActivity;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public class MotionAnalyzerA implements MotionAnalyzer {

    private static final String TAG = MotionAnalyzerA.class.getName();

    @Override
    public Vec2f getRelativeMotionVector(Bitmap current, OpenCVHandler openCVHandler) {
        if(current != null) {
            Point[] points = openCVHandler.calculateOpticalFlowPyrLK(current);
            if(points != null) {
                for (Point p : points) {
                    Log.d(TAG, "Point: " + p.x + "|" + p.y);
                }
            }
            Log.d(TAG, "__________________________________________________________");
            return new Vec2f(0,0);
        }
        else{
            Log.d(TAG, "Bitmap NULL!");
            return null;
        }
    }
}
