package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.media.Image;
import android.media.ImageReader;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.core.Const;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.ImageProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.OptFlowFeature;
import de.tu_chemnitz.tomkr.augmentedmaps.view.ARActivity;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.debug;

/**
 * Created by Tom Kretzschmar on 21.12.2017.<br>
 * <br>
 * A Sensor implementation which doesn't use an internal system sensor but uses camera images and OpenCV image analysis to get current rotation readings.<br>
 * Rotations are integrated to get the absolute orientation values resulting from an initial rotation estimate.<br>
 * The Rotation is gained through extracted motion vectors which are checked for reliability against gyroscope readings.
 */

public class OptFlowSensor implements Sensor, ImageProcessor {
    /**
     * Tag for logging
     */
    private static final String TAG = OptFlowSensor.class.getName();

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
            System.exit(1);
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    /**
     * The size of the downscaled image to process
     */
    private Size targetSize;

    /**
     * The gyroscope sensor instance for reliablity check
     */
    private Sensor gyroSensor;

    /**
     * The current gyro rotation
     */
    private float[] gyroRotation;

    /**
     * The delta between the current and the last gyroscope reading
     */
    private float[] gyroDelta;

    /**
     * Actual resulting rotation/orientation of the device
     */
    private float[] rotation;

    /**
     * Extracted feature points with positions from the last update
     */
    private Point[] prevPts;

    /**
     * Extracted feature points with positions from the current update
     */
    private Point[] currPts;

    /**
     * Input image width
     */
    private int width;

    /**
     * Input image height
     */
    private int height;

    /**
     * The image from the last update
     */
    private Mat oldImage;

    /**
     * The image from the current update
     */
    private Mat currentImage;

    /**
     * Flag to reset the feature points
     */
    private boolean reset;

    /**
     * Flag to pause the update loop
     */
    private boolean pause;

    private List<OptFlowFeature> optFlowFeaturesToDraw;

    /**
     * Full constructor.
     * @param gyroSensor GyroSensor used for reliability check
     */
    public OptFlowSensor(Sensor gyroSensor) {
        this.gyroSensor = gyroSensor;
    }

    @Override
    public float[] getRotation() {
        return rotation;
    }

    @Override
    public void setRotationEstimate(float[] rotation) {
        this.rotation = rotation;
    }

    @Override
    public void start() {
        optFlowFeaturesToDraw = new ArrayList<>();
        pause = false;
    }

    @Override
    public void pause() {
        pause = true;
        optFlowFeaturesToDraw = null;
        reset = true;
    }

    /**
     * Callback function from ImageReader. Called when new image is available for processing.
     * @param imageReader The imageReader instance providing the new image.
     */
    @Override
    public void onImageAvailable(ImageReader imageReader) {
        Image image = imageReader.acquireLatestImage();
        byte[] bytes = null;
        if(!pause) {
            width = image.getWidth();
            height = image.getHeight();
            ByteBuffer buffer = image.getPlanes()[0].getBuffer(); // get luminance plane from YUV_420_888 image; only greyscale needed for calculations
            synchronized (this) {
                bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
            }
        }
        image.close();

        if (rotation != null && !pause) {
            currentImage = new Mat();
            Mat color = new Mat(height, width, CvType.CV_8UC1);
            color.put(0, 0, bytes);

            if (targetSize == null) {
                targetSize = new Size(color.width() / Const.IMAGE_SCALING_FACTOR, color.height() / Const.IMAGE_SCALING_FACTOR); // calculate with half size
            }

            Imgproc.resize(color, currentImage, targetSize);

            float[] newGyroRotation = gyroSensor.getRotation();
            if (gyroRotation != null) {
                gyroDelta[0] = gyroRotation[0] - newGyroRotation[0];
                gyroDelta[1] = gyroRotation[1] - newGyroRotation[1];
                gyroRotation = newGyroRotation;
            } else {
                gyroRotation = newGyroRotation;
                gyroDelta = new float[]{0, 0};
            }

            reset = (prevPts == null || oldImage == null || reset);
            if (reset) {
                initFeaturePoints();
                reset = false;
            } else {
                updateFeaturePoints();
                List<float[]> motionVecs = getMotionVecs();
                if (motionVecs.size() < Const.MIN_TRACKING_POINTS) {
                    reset = true;
                }
                reliabilityComp(motionVecs);
                float[] deltaRotation = aggregateMotionVecs(motionVecs);
                rotation[0] = rotation[0] + deltaRotation[0];
                rotation[1] = rotation[1] + deltaRotation[1];
            }
            oldImage = currentImage;
            prevPts = currPts;
        }
//        Log.d(TAG, "update Debug Points");
        if (ARActivity.getView() != null) ARActivity.getView().setOptFlowFeaturesToDraw(optFlowFeaturesToDraw);
    }

    /**
     * Use OpenCV Improc.goodFeaturesToTrack to acquire initial feature points which can be tracked afterwards.
     */
    private void initFeaturePoints() {
        Log.d(TAG, "initFeaturePoints");
        MatOfPoint initial = new MatOfPoint();
        Imgproc.goodFeaturesToTrack(currentImage, initial, Const.MAX_TRACKING_POINTS, 0.1, 30);
        MatOfPoint2f featurePoints = new MatOfPoint2f();
        initial.convertTo(featurePoints, CvType.CV_32F);
        currPts = featurePoints.toArray();
    }

    /**
     * Use OpenCV Video.calcOpticalFlowPyrLK to track given feature points.
     */
    private void updateFeaturePoints() {
//            Log.d(TAG, "updateFeaturePoints");
        MatOfByte status = new MatOfByte();
        MatOfFloat err = new MatOfFloat();
        MatOfPoint2f newPoints = new MatOfPoint2f();

        Video.calcOpticalFlowPyrLK(oldImage, currentImage, new MatOfPoint2f(prevPts), newPoints, status, err);
        currPts = newPoints.toArray();
    }

    /**
     * Calculate motion vectors from previous and current feature point positions.
     * @return Motion vectors with relative values in the interval [0..1]
     */
    private List<float[]> getMotionVecs() {
        List<float[]> vecs = new ArrayList<>();
        if(debug && !optFlowFeaturesToDraw.isEmpty()) {
            optFlowFeaturesToDraw.clear();
        }
        for (int i = 0; i < currPts.length; i++) {
            Point c = currPts[i];
            Point p = prevPts[i];
            if (c.x < targetSize.width && c.y < targetSize.height && c.x > 0 && c.y > 0) {
                vecs.add(new float[]{(float) (p.x - c.x), (float) (p.y - c.y)});
                if(debug) {
                    optFlowFeaturesToDraw.add(new OptFlowFeature(currPts[i].x * Const.IMAGE_SCALING_FACTOR, currPts[i].y * Const.IMAGE_SCALING_FACTOR, prevPts[i].x * Const.IMAGE_SCALING_FACTOR, prevPts[i].y * Const.IMAGE_SCALING_FACTOR));
                }
            }
        }
        for (float[] v : vecs) {
            v[0] = v[0] * Const.IMAGE_SCALING_FACTOR * Camera2.fov[0] / width;
            v[1] = v[1] * Const.IMAGE_SCALING_FACTOR * Camera2.fov[1] / height;
        }
        return vecs;
    }

    /**
     * Check if motion vectors are reliable using the current gyroscope sensor readings. Removes unreliable vectors.
     * @param vecs The motion vectors to check.
     */
    private void reliabilityComp(List<float[]> vecs) {
        int i = 0;
        for (Iterator<float[]> it = vecs.iterator(); it.hasNext(); ) {

            float[] vec = it.next();
//            Log.d(TAG, "reliability: " + vec[0] + "<>" + gyroDelta[0]);
            if (Math.abs(vec[0] - gyroDelta[0]) > Const.RELIABILITY_THRESHOLD || Math.abs(vec[1] - gyroDelta[1]) > Const.RELIABILITY_THRESHOLD) {
                it.remove();
                if(debug) {
                    optFlowFeaturesToDraw.get(i).setReliable(false);
                }
                Log.d(TAG, "unreliable vector: " + vec[0] + " - " + gyroDelta[0]);
            } else if(debug){
                optFlowFeaturesToDraw.get(i).setReliable(true);
            }
            i++;
        }
    }

    /**
     * Aggregate all motion vectors using a simple density function implementation.
     *
     * @return Resulting aggregated motion vector
     */
    private float[] aggregateMotionVecs(List<float[]> vecs) {
        float[] result = new float[]{0, 0};
        float[] max = new float[]{0, 0};
        for (float[] v : vecs) {
            float[] count = new float[]{0, 0};
            for (float[] w : vecs) {
                if (Math.abs(v[0] - w[0]) < Const.AGGREGATION_THRESHOLD) {
                    count[0]++;
                }
                if (Math.abs(v[1] - w[1]) < Const.AGGREGATION_THRESHOLD) {
                    count[1]++;
                }
            }
            if (count[0] > max[0]) {
                max[0] = count[0];
                result[0] = v[0];
            }
            if (count[1] > max[1]) {
                max[1] = count[1];
                result[1] = v[1];
            }
        }
        Log.d(TAG, "aggregatedMotionVecs: (" + vecs.size() + ") -> " + result[0] + "|" + result[1]);
        return result;
    }
}
