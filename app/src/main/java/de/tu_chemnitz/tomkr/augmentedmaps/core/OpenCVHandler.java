package de.tu_chemnitz.tomkr.augmentedmaps.core;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.SparsePyrLKOpticalFlow;
import org.opencv.video.Video;

import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

import static android.R.attr.maxLevel;
import static android.R.attr.width;
import static android.os.Build.VERSION_CODES.M;


/**
 * Created by Tom Kretzschmar on 18.10.2017.
 *
 */

public class OpenCVHandler {

    private static final String TAG = OpenCVHandler.class.getName();

    static {
        if(!OpenCVLoader.initDebug()){
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

    public OpenCVHandler(){
        featurePoints = new MatOfPoint2f();
    }

    private MatOfPoint calculateFeatureSet(Mat current){
        double quality = 1;
        double minDist = 10;
        MatOfPoint corners = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(current, corners, 5 /*max Corners*/, quality, minDist);
        return corners;
    }

    public Point[] calculateOpticalFlowPyrLK(Bitmap bmp){// below is working
//        Log.d(TAG, " " + bmp.getWidth() + " x " + bmp.getHeight());
        Mat current = new Mat();
        Mat colorX = new Mat();

//        Mat color = new Mat();
//        Utils.bitmapToMat(bmp, color);

        Utils.bitmapToMat(bmp, colorX);

//        Size targetSize = new Size(colorX.width()/2f, colorX.height()/2f);

        Size targetSize = new Size(colorX.width(), colorX.height());

        Core.flip(colorX.t(), color, 1);
        Imgproc.resize(color, color, targetSize);


        Imgproc.cvtColor(color, current, Imgproc.COLOR_RGBA2GRAY);

        boolean resetFeatures = false;
        for(Point p : featurePoints.toArray()){
            if(p.x > color.size().width || p.y > color.size().height || p.x < 0 || p.y < 0){
                resetFeatures = true;
            }
        }

        if(oldImage == null || resetFeatures || featurePoints.empty()) {
            init = true;
            Log.d(TAG, "INIT FEATURE_POINTS because of img: " + (oldImage == null) + " reset: " + resetFeatures + " or empty points: " + (featurePoints.empty()));
            MatOfPoint initial = new MatOfPoint();
//            initial.fromArray(new Point(960,540));
            Imgproc.goodFeaturesToTrack(current, initial, 5, 0.1, 30);
            initial.convertTo(featurePoints, CvType.CV_32F);
            this.oldImage = current;
        } else {
            init = false;
            status = new MatOfByte();
            err = new MatOfFloat();
            MatOfPoint2f newFeaturePoints = new MatOfPoint2f();

            Video.calcOpticalFlowPyrLK(oldImage, current, featurePoints, newFeaturePoints, status, err);
//            Point[] points = featurePoints.toArray();
//            float[] error = err.toArray();

//            Log.d(TAG, "Point1: " + points[0].x + "|" + points[0].y + " Err: " + error[0]);

            this.featurePoints = newFeaturePoints;
            this.oldImage = current;
        }
        return featurePoints.toArray(); // TODO: remap coordinate system!!! correct now, but slow. wrong initially because of transform of textureView preview. maybe get bitmap / data directly from bytes, better features?
    }

    public boolean getInit(){
        return init;
    }

    public Vec2f calculateOpticalFlow(Bitmap bmp, int w, int h){
//        Mat current = new Mat();
//        Mat color = new Mat();
//        Utils.bitmapToMat(bmp, color);
//        Imgproc.cvtColor(color, current, Imgproc.COLOR_RGBA2GRAY);
////        if(old != null) {
////            Size winSize = new Size(w, h);
////            int maxLevel = 3;
////            TermCriteria crit = new TermCriteria();
////            int flags = 0;
////            double minEigThreshold = 1;
////
////
////            Mat nextPts = new Mat();
////
////
//////        SparsePyrLKOpticalFlow.create(winSize, maxLevel, crit, flags, minEigThreshold);
////            SparsePyrLKOpticalFlow sparsePyrLKOpticalFlow = SparsePyrLKOpticalFlow.create();
////            sparsePyrLKOpticalFlow.calc(old, current, pts, nextPts, status);
////
////            this.old = current;
//////            this.pts = nextPts;
////
////            return null;
////        } else {
//            this.pts = calculateFeatureSet(current);
//            this.old = current;
//            return new Vec2f(0,0);
////        }
        return null;
    }


    private void detectEdges(Bitmap bitmap) {
        Mat rgba = new Mat();
        Utils.bitmapToMat(bitmap, rgba);

        Mat edges = new Mat(rgba.size(), CvType.CV_8UC1);
        Imgproc.cvtColor(rgba, edges, Imgproc.COLOR_RGB2GRAY, 4);
        Imgproc.Canny(edges, edges, 80, 100);

        // Don't do that at home or work it's for visualization purpose.
//        BitmapHelper.showBitmap(this, bitmap, imageView);
        Bitmap resultBitmap = Bitmap.createBitmap(edges.cols(), edges.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(edges, resultBitmap);
//        BitmapHelper.showBitmap(this, resultBitmap, detectEdgesImageView);
//        ImageView img = (ImageView) findViewById(R.id.imageView);
////        Canvas canvas = new Canvas(resultBitmap);
//        img.setImageBitmap(resultBitmap);
//        img.invalidate();
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
        for(int row = 0; row<grayscale.rows(); row++){
            int sumCurrentRow = 0;
            for(int col = 0; col<grayscale.cols(); col++){
                double dot = grayscale.get(row, col)[0];
                sumCurrentRow += (int)dot;
            }

            if(row != 0){
                int currentDiff = Math.abs(sumLastRow - sumCurrentRow);
                if(currentDiff > maxDiff){
                    maxDiff = currentDiff;
                    maxDiffRow = row;
                }
            }
            sumLastRow = sumCurrentRow;
        }

        for(int col = 0; col < rgba_small.cols(); col++){
            rgba_small.put(maxDiffRow, col, 255,0,0, 255);
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
        for(int row = 0; row<grayscale.rows(); row+=interval){
            int sumCurrentRows = 0;
            for(int k = 0; k<interval; k++){
                for(int col = 0; col<grayscale.cols(); col++){
                    double dot = grayscale.get(row, col)[0];
                    sumCurrentRows += (int)dot;
                }
            }
            if(row != 0){
                int currentDiff = Math.abs(sumLastRows - sumCurrentRows);
                if(currentDiff > maxDiff){
                    maxDiff = currentDiff;
                    maxDiffRowStart = row;
                }
            }
            sumLastRows = sumCurrentRows;
        }

        for(int col = 0; col < rgba_small.cols(); col++){
            rgba_small.put(maxDiffRowStart > 0 ? maxDiffRowStart - 1 : 0, col, 255,0,0, 255);
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
