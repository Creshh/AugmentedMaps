package de.tu_chemnitz.tomkr.augmentedmaps.core;

import android.graphics.Color;
import android.graphics.Paint;

import de.tu_chemnitz.tomkr.augmentedmaps.R;

/**
 * Created by Tom Kretzschmar on 26.10.2017.
 *
 * A collection of all used constants in the scope of this project (de.tu_chemnitz.tomkr.augmentedmaps).
 */
@SuppressWarnings("WeakerAccess")
public class Const {

    /**
     * Paint object for drawing in {@link de.tu_chemnitz.tomkr.augmentedmaps.view.ARView}.
     */
    public static Paint paintStroke;

    /**
     * Paint object for drawing in {@link de.tu_chemnitz.tomkr.augmentedmaps.view.ARView}.
     */
    public static Paint paintFill;

    /**
     * Paint object for drawing in {@link de.tu_chemnitz.tomkr.augmentedmaps.view.ARView}.
     */
    public static Paint paintFillRed;

    static {
        paintStroke = new Paint();
        paintStroke.setColor(Color.GREEN);
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setStrokeWidth(2);
        paintStroke.setAntiAlias(true);
        paintStroke.setAlpha(255);

        paintFill = new Paint();
        paintFill.setColor(Color.GREEN);
        paintFill.setStyle(Paint.Style.FILL);
        paintFill.setAntiAlias(true);
        paintFill.setAlpha(255);
        paintFill.setTextAlign(Paint.Align.LEFT);
        paintFill.setTextSize(40);
        paintFill.setAntiAlias(true);

        paintFillRed = new Paint(paintFill);
        paintFillRed.setColor(Color.RED);
    }

    /**
     * Config resource file identifier.
     */
    public static final int CONFIG_FILE_RESOURCE = R.raw.config;

    /**
     * Maximum frames/ticks per second. Used for {@link LooperThread} implementations.
     */
    public static final int FPS = 20;

    /**
     * Resulting time per frame. Do not change this value, change FPS instead.
     */
    public static final int TARGET_FRAMETIME = 1000 / FPS;

    /**
     * Duration in ms for logging orientation values to file.
     */
    public static final int LOG_TIME = 30 * 1000;

    /**
     * Maximum distance in meters in which mapnodes are allocated.
     */
    public static final int MAX_DISTANCE = 10000;

    /**
     * Distance in meters after which new mapnodes are allocated.
     */
    public static final float DIST_THRESHOLD = 500;

    /**
     * Message for updating the whole ARView.
     */
    public static final int MSG_UPDATE_VIEW = 1;

    /**
     * Message for updating the current location debug view.
     */
    public static final int MSG_UPDATE_LOC_VIEW = 2;

    /**
     * Message for updating the current orientation debug view.
     */
    public static final int MSG_UPDATE_ORIENTATION_VIEW = 3;

    /**
     * Message for updating the map nodes.
     */
    public static final int MSG_UPDATE_MAPNODES = 4;

    /**
     * Message for updating the point positions with the corresponding data processor.
     */
    public static final int MSG_PROCESS_DATA = 5;

    /**
     * Message for querying the own height.
     */
    public static final int MSG_UPDATE_OWN_HEIGHT = 6;

    /**
     * Message for updating the point heights.
     */
    public static final int MSG_UPDATE_NODE_HEIGHT = 7;

    /**
     * Message for updating the current state debug view.
     */
    public static final int MSG_UPDATE_STATE_VIEW = 8;

    /**
     * Message for updating the current fps debug view.
     */
    public static final int MSG_UPDATE_FPS_VIEW = 9;

    /**
     * Message for updating the info debug view.
     */
    public static final int MSG_UPDATE_INFO_VIEW = 10;

    /**
     * Minimum time interval between location updates, in milliseconds. See {@link android.location.LocationManager}
     */
    public static final int LOCATION_UPDATE_INTERVAL = 1000;

    /**
     * Minimum distance between location updates, in meters. See {@link android.location.LocationManager}
     */
    public static final float LOCATION_UPDATE_DISTANCE = 10f;

    /**
     * Factor for low pass filter in {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService}.
     */
    public static final float LOW_PASS_FAC = 0.20f;

    /**
     * Factor for gyroscope value in complementary filter in {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService}.
     */
    public static final float GYRO_FAC = 0.99f;

    /**
     * Factor for optical flow value in complementary filter in {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService}.
     */
    public static final float OPTFLOW_FAC = 0.99f;

    /**
     * Factor of downscaling image for processing in {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OptFlowSensor}.
     */
    public static final float IMAGE_SCALING_FACTOR = 4f;

    /**
     * Maximum or targeted count of tracking points in {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OptFlowSensor}.
     */
    public static final int MAX_TRACKING_POINTS = 15;

    /**
     * Minimum count of tracking points available, before new tracking points are allocated in {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OptFlowSensor}.
     */
    public static final int MIN_TRACKING_POINTS = 5;

    /**
     * Maximum allowed diversity between gyroscope value and motion vector to set this vector as reliable in {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OptFlowSensor}.
     */
    public static final float RELIABILITY_THRESHOLD = 5.0f;

    /**
     * Threshold for aggregation density function in {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OptFlowSensor}.
     */
    public static final float AGGREGATION_THRESHOLD = 0.04f;

    /**
     * Flag for showing debug information
     */
    public static final boolean debug = true;
}
