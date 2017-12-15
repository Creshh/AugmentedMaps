package de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing;

import android.graphics.Bitmap;
import android.media.ImageReader;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

/**
 * Created by Tom Kretzschmar on 14.12.2017.
 *
 */

public class HorizonTracker implements ImageProcessor{


    @Override
    public Vec2f getRelativeMotionAngles(float[] fov) {
        return null;
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




    @Override
    public void onImageAvailable(ImageReader imageReader) {

    }
}
