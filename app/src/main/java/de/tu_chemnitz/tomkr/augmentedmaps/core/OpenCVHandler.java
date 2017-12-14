package de.tu_chemnitz.tomkr.augmentedmaps.core;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;
import de.tu_chemnitz.tomkr.augmentedmaps.core.Constants;


/**
 * Created by Tom Kretzschmar on 18.10.2017.
 *
 */

public class OpenCVHandler {

    private static final String TAG = OpenCVHandler.class.getName();

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
            System.exit(1);
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    private Mat color = new Mat();
    private Mat oldImage;
    private MatOfPoint2f featurePoints;
    private MatOfByte status;
    private MatOfFloat err;
    private boolean init = false;
    private boolean reset = false;

    public OpenCVHandler() {
        featurePoints = new MatOfPoint2f();
    }

    public Point[] calculateOpticalFlowPyrLK(byte[] bytes, int width, int height) {// below is working
        Mat current = new Mat();

        color  = new Mat(height, width, CvType.CV_8UC1);
        color.put(0,0, bytes);

//        Size targetSize = color.size();
        Size targetSize = new Size(color.width() / Constants.IMAGE_SCALING_FACTOR, color.height() / Constants.IMAGE_SCALING_FACTOR); // calculate with half size
        Imgproc.resize(color, current, targetSize);




        if (oldImage == null || reset || featurePoints.empty()) {
            Log.d(TAG, "INIT FEATURE_POINTS because of img: " + (oldImage == null) + " reset: " + reset + " or empty points: " + (featurePoints.empty()));
            reset = false;
            MatOfPoint initial = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(current, initial, Constants.MAX_TRACKING_POINTS, 0.1, 30);
            initial.convertTo(featurePoints, CvType.CV_32F);
            this.oldImage = current;
        } else {
            init = false;
            status = new MatOfByte();
            err = new MatOfFloat();
            MatOfPoint2f newFeaturePoints = new MatOfPoint2f();

            Video.calcOpticalFlowPyrLK(oldImage, current, featurePoints, newFeaturePoints, status, err);


            for (Point p : featurePoints.toArray()) {
                if (p.x > targetSize.width || p.y > targetSize.height || p.x < 0 || p.y < 0) {
                    init = true;
                    reset = true;
                }
            }

            this.featurePoints = newFeaturePoints;
            this.oldImage = current;
        }

        Point[] points = featurePoints.toArray();
//        Log.d(TAG, "Point1: " + points[0].x + "|" + points[0].y);

        for (Point p : points) {
            p.x = p.x * Constants.IMAGE_SCALING_FACTOR;
            p.y = p.y * Constants.IMAGE_SCALING_FACTOR;
        }

        return points;
    }

    public boolean getInit() {
        return init;
    }




    private void detectHorizon(Bitmap bitmap) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        Size size = new Size(rgba.width() * 0.5f, rgba.height() * 0.5f);
        Mat rgba_small = new Mat(size, rgba.type());

        Imgproc.resize(rgba, rgba_small, size);

        Mat grayscale = new Mat(rgba_small.size(), CvType.CV_8UC1);

        Imgproc.cvtColor(rgba_small, grayscale, Imgproc.COLOR_RGB2GRAY, 4);


        int sumLastRow = 0;
        int maxDiff = 0;
        int maxDiffRow = 0;
        for (int row = 0; row < grayscale.rows(); row++) {
            int sumCurrentRow = 0;
            for (int col = 0; col < grayscale.cols(); col++) {
                double dot = grayscale.get(row, col)[0];
                sumCurrentRow += (int) dot;
            }

            if (row != 0) {
                int currentDiff = Math.abs(sumLastRow - sumCurrentRow);
                if (currentDiff > maxDiff) {
                    maxDiff = currentDiff;
                    maxDiffRow = row;
                }
            }
            sumLastRow = sumCurrentRow;
        }

        for (int col = 0; col < rgba_small.cols(); col++) {
            rgba_small.put(maxDiffRow, col, 255, 0, 0, 255);
        }


        // Don't do that at home or work it's for visualization purpose.
//        BitmapHelper.showBitmap(this, bitmap, imageView);
        Bitmap resultBitmap = Bitmap.createBitmap(rgba_small.cols(), rgba_small.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba_small, resultBitmap);
//        BitmapHelper.showBitmap(this, resultBitmap, detectEdgesImageView);
//        ImageView img = (ImageView) findViewById(R.id.imageView);
//        Canvas canvas = new Canvas(resultBitmap);
//        img.setImageBitmap(resultBitmap);
//        img.invalidate();
    }

    private void detectHorizon2(Bitmap bitmap) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        Size size = new Size(rgba.width() * 0.5f, rgba.height() * 0.5f);
        Mat rgba_small = new Mat(size, rgba.type());

        Imgproc.resize(rgba, rgba_small, size);

        Mat grayscale = new Mat(rgba_small.size(), CvType.CV_8UC1);

        Imgproc.cvtColor(rgba_small, grayscale, Imgproc.COLOR_RGB2GRAY, 4);


        int interval = 5;
        int sumLastRows = 0;
        int maxDiff = 0;
        int maxDiffRowStart = 0;
        for (int row = 0; row < grayscale.rows(); row += interval) {
            int sumCurrentRows = 0;
            for (int k = 0; k < interval; k++) {
                for (int col = 0; col < grayscale.cols(); col++) {
                    double dot = grayscale.get(row, col)[0];
                    sumCurrentRows += (int) dot;
                }
            }
            if (row != 0) {
                int currentDiff = Math.abs(sumLastRows - sumCurrentRows);
                if (currentDiff > maxDiff) {
                    maxDiff = currentDiff;
                    maxDiffRowStart = row;
                }
            }
            sumLastRows = sumCurrentRows;
        }

        for (int col = 0; col < rgba_small.cols(); col++) {
            rgba_small.put(maxDiffRowStart > 0 ? maxDiffRowStart - 1 : 0, col, 255, 0, 0, 255);
        }


        // Don't do that at home or work it's for visualization purpose.
//        BitmapHelper.showBitmap(this, bitmap, imageView);
        Bitmap resultBitmap = Bitmap.createBitmap(rgba_small.cols(), rgba_small.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(rgba_small, resultBitmap);
//        BitmapHelper.showBitmap(this, resultBitmap, detectEdgesImageView);
//        ImageView img = (ImageView) findViewById(R.id.imageView);
////        Canvas canvas = new Canvas(resultBitmap);
//        img.setImageBitmap(resultBitmap);
//        img.invalidate();
    }

}
