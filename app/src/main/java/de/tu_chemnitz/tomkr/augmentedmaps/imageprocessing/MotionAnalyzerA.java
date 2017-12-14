package de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing;

import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import org.opencv.core.Point;

import java.nio.ByteBuffer;

import de.tu_chemnitz.tomkr.augmentedmaps.core.OpenCVHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;
import de.tu_chemnitz.tomkr.augmentedmaps.view.ARActivity;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public class MotionAnalyzerA implements MotionAnalyzer{

    private static final String TAG = MotionAnalyzerA.class.getName();

    private Point[] prevPts;

    private byte[] current;

    @Override
    public Vec2f getRelativeMotionVector(OpenCVHandler openCVHandler) {
        if(current != null) {
            Point[] points = openCVHandler.calculateOpticalFlowPyrLK(current);
            if(points != null) {
                for (Point p : points) {
//                    Log.d(TAG, "Point: " + p.x + "|" + p.y);
                }
                if (ARActivity.getView() != null) ARActivity.getView().setDebugArray(points.clone());

//            Log.d(TAG, "__________________________________________________________");


                Vec2f vec = null;
                Vec2f[] vecs = new Vec2f[points.length];
                if (!openCVHandler.getInit() && prevPts != null && prevPts.length > 0 && points.length > 0 && prevPts.length == points.length) {
                    Log.d(TAG, "try match");
                    for(int i = 0; i < points.length; i++){
                        vecs[i] = new Vec2f((float)(prevPts[i].x - points[i].x), (float)(prevPts[i].y - points[i].y));
                    }

                    float sumX = 0;
                    float sumY = 0;
                    float count = 0;
                    for(Vec2f v : vecs){
//                        if(v.getX() < 200 && v.getY() < 200){ // define thresholds
                            sumX += v.getX();
                            sumY += v.getY();
                            count++;
//                        }
                    }
                    vec = new Vec2f((sumX / count) / 1920, (sumY / count)/ 1080);
                    ARActivity.getView().setDebugVec(new Vec2f((sumX / count),(sumY /count)));
                }
                prevPts = points;
                return vec;
            }
            return new Vec2f(0,0);
        }
        else{
            Log.d(TAG, "Bitmap NULL!");
            return null;
        }
    }

    @Override
    public void onImageAvailable(ImageReader imageReader) {
        Log.d(TAG, "onImageAvailable");
        Image image = imageReader.acquireLatestImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer(); // get luminance plane from YUV_420_888 image
        synchronized (this) {
            current = new byte[buffer.remaining()];
            buffer.get(current);
        }
        image.close();
    }
}
