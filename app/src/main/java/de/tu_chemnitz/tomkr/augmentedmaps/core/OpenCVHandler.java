package de.tu_chemnitz.tomkr.augmentedmaps.core;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


/**
 * Created by Tom Kretzschmar on 18.10.2017.
 *
 */

public class OpenCVHandler {

    private static final String TAG = OpenCVHandler.class.getName();

    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    private Bitmap old;

    private void calculateOpticalFlow(Bitmap current){

        Imgproc.

        this.old = current;
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
