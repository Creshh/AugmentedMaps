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

import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.core.Constants;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.ImageProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;
import de.tu_chemnitz.tomkr.augmentedmaps.view.ARActivity;

/**
 * Created by Tom Kretzschmar on 21.12.2017.
 *
 */

public class OptFlowSensor implements Sensor, ImageProcessor {
    private static final String TAG = OptFlowSensor.class.getName();

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
            System.exit(1);
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    private Sensor gyroSensor;
    private float[] gyroRotation;
    private float[] rotation;

    private Point[] prevPts;
    private int width;
    private int height;
    private byte[] current;
    private Mat oldImage;
    private MatOfPoint2f featurePoints = new MatOfPoint2f();
    private boolean init = false;
    private boolean reset = false;

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
    public void onImageAvailable(ImageReader imageReader) {
        Image image = imageReader.acquireLatestImage();
        width = image.getWidth();
        height = image.getHeight();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer(); // get luminance plane from YUV_420_888 image; only greyscale needed for calculations
        synchronized (this) {
            current = new byte[buffer.remaining()];
            buffer.get(current);
        }
        image.close();

        updateRotation();
    }

    private void updateRotation() {

        if (current != null) {
            Point[] points = calculateOpticalFlowPyrLK(current, width, height);
            if (points != null) {
                if (ARActivity.getView() != null) ARActivity.getView().setDebugArray(points.clone());

                float[] vec = new float[2];
                Vec2f[] vecs = new Vec2f[points.length];
                if (!init && prevPts != null && prevPts.length > 0 && points.length > 0 && prevPts.length == points.length) {
                    for (int i = 0; i < points.length; i++) {
                        vecs[i] = new Vec2f((float) (prevPts[i].x - points[i].x), (float) (prevPts[i].y - points[i].y));
                    }

                    float sumX = 0;
                    float sumY = 0;
                    float count = 0;
                    for (Vec2f v : vecs) {
                        if (v.getX() < 200 && v.getY() < 200) { // define thresholds
                            sumX += v.getX();
                            sumY += v.getY();
                            count++;
                        }
                    }

                    vec[0] = ((sumX / count) / width) * Camera2.fov[0];
                    vec[1] = ((sumY / count) / height) * Camera2.fov[1];
                    if (vec[1] > 3 || vec[0] > 3) {
                        Log.e(TAG, "-------------- MOTION VECTOR TOO BIG ------------");
                    }
                    ARActivity.getView().setDebugVec(new Vec2f((sumX / count), (sumY / count)));
                }
                prevPts = points;

                if (init) {
                    Log.e(TAG, "MOTION VECTOR -> INIT");
                }
                rotation[0] += vec[0];
                rotation[1] += vec[1];
            }
        }
    }


    // TODO: dont move feature points but use a fixed amount of points -> calulate a vector field and return that instead of new points. this way no new initalisation has to be done!
    // TODO: new initialization after 35% of points lost. - move points with "features"
    private Point[] calculateOpticalFlowPyrLK(byte[] bytes, int width, int height) {// below is working
        Mat current = new Mat();
        Mat color = new Mat(height, width, CvType.CV_8UC1);
        color.put(0, 0, bytes);

        Size targetSize = new Size(color.width() / Constants.IMAGE_SCALING_FACTOR, color.height() / Constants.IMAGE_SCALING_FACTOR); // calculate with half size
        Imgproc.resize(color, current, targetSize);

        if (oldImage == null || reset || featurePoints.empty()) {
//            Log.d(TAG, "INIT FEATURE_POINTS because of img: " + (oldImage == null) + " reset: " + reset + " or empty points: " + (featurePoints.empty()));
            reset = false;
            MatOfPoint initial = new MatOfPoint();
            Imgproc.goodFeaturesToTrack(current, initial, Constants.MAX_TRACKING_POINTS, 0.1, 30);
            initial.convertTo(featurePoints, CvType.CV_32F);
            this.oldImage = current;
        } else {
            init = false;
            MatOfByte status = new MatOfByte();
            MatOfFloat err = new MatOfFloat();
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

        for (Point p : points) {
            p.x = p.x * Constants.IMAGE_SCALING_FACTOR;
            p.y = p.y * Constants.IMAGE_SCALING_FACTOR;
        }
        return points;
    }
}
