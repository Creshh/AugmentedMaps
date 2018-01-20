package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.content.Context;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import de.tu_chemnitz.tomkr.augmentedmaps.core.Const;
import de.tu_chemnitz.tomkr.augmentedmaps.core.LooperThread;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.ImageProcessor;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.LOW_PASS_FAC;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.TARGET_FRAMETIME;

/**
 * Created by Tom Kretzschmar on 21.12.2017.<br>
 * <br>
 * Service which provides continous user device location updates using different configurable sensors.<br>
 * Implements sensor fusion using {@link AccMagSensor}, {@link GyroSensor} and {@link OptFlowSensor}.
 */
public class OrientationService extends LooperThread {
    /**
     * Tag for logging
     */
    private static final String TAG = OrientationService.class.getName();

    /**
     * Enum type which is used to define the sensors used in the actual sensor fusion.
     */
    public enum Flag {RAW, LOW_PASS, GYRO, OPT_FLOW}

    /**
     * Actual {@link Flag} set and used by fusion.
     */
    private Flag flag;

    /**
     * Flag set to true before first orientaiton value is acquired.
     */
    private boolean init;

    /**
     * A List of {@link OrientationService} which are notified after each orientation update.
     */
    private List<OrientationListener> listeners;

    /**
     * {@link AccMagSensor} instance.
     */
    private Sensor accMagSensor;

    /**
     * {@link GyroSensor} instance.
     */
    private Sensor gyroSensor;

    /**
     * {@link OptFlowSensor} instance.
     */
    private Sensor optFlowSensor;

    /**
     * Current device rotation.
     */
    private float[] rotation;

    /**
     * Full constructor. Initializes all used sensors and set {@link Flag} RAW.
     * @param context Application or Activity context
     */
    public OrientationService(Context context) {
        super(TARGET_FRAMETIME);
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accMagSensor = new AccMagSensor(sensorManager);
        gyroSensor = new GyroSensor(sensorManager);
        optFlowSensor = new OptFlowSensor(gyroSensor);
        listeners = new ArrayList<>();
        init = true;
        flag = Flag.RAW;
    }


    /**
     * Main orientation loop which accumulates all different sensor values and fuse the values according to the set flag.
     */
    @Override
    protected void loop() {
        float[] accMag;
        float[] gyro;
        float[] optFlow;

        if (init) {
            Log.d(TAG, "init");
            rotation = accMagSensor.getRotation();
            init = (rotation == null);
        } else {
            accMag = Arrays.copyOf(accMagSensor.getRotation(), 3);
            switch (flag) {
                case RAW:
                    rotation = accMag;
                    break;
                case LOW_PASS:
                    for (int i = 0; i < 3; i++) {
                        // Complementary Filter with AccMagSensor
                        rotation[i] = (rotation[i] * (1 - LOW_PASS_FAC)) + (accMag[i] * LOW_PASS_FAC);
                    }
                    break;
                case GYRO:
                    gyro = gyroSensor.getRotation();
                    for (int i = 0; i < 3; i++) {
                        // Complementary Filter with AccMagSensor and Gyroscope
                        rotation[i] = (gyro[i] * Const.GYRO_FAC) + (accMag[i] * (1 - Const.GYRO_FAC));
                    }
                    break;
                case OPT_FLOW:
                    optFlow = optFlowSensor.getRotation();
                    for (int i = 0; i < 3; i++) {
                        // Complementary Filter with AccMagSensor and Optical Flow
                        rotation[i] = (optFlow[i] * Const.OPTFLOW_FAC) + (accMag[i] * (1 - Const.OPTFLOW_FAC));
                    }
                    break;
            }
            gyroSensor.setRotationEstimate(Arrays.copyOf(rotation, 3));
            optFlowSensor.setRotationEstimate(Arrays.copyOf(rotation, 3));
            notifyListeners(new Orientation(rotation[0], rotation[1], rotation[2]));
        }
    }

    @Override
    protected void onStart() {
        toggleSensors(flag);
    }

    @Override
    protected void onPause() {
        accMagSensor.pause();
        gyroSensor.pause();
        optFlowSensor.pause();
    }

    /**
     * Call all registered listeners with current device orientation.
     * @param orientation The current device orientation
     */
    private void notifyListeners(Orientation orientation) {
        for (OrientationListener listener : listeners) {
            listener.onOrientationChange(orientation);
        }
    }

    /**
     * Register a {@link OrientationListener} to get continuous orientation updates.
     * @param listener The listener instance to be registered.
     */
    public void registerListener(OrientationListener listener) {
        this.listeners.add(listener);
    }

    /**
     * Remove a {@link OrientationListener} from the service.
     * @param listener The listener instance to be unregistered.
     */
    public void unregisterListener(OrientationListener listener) {
        listeners.remove(listener);
    }

    /**
     * Set the {@link Flag} for sensor fusion.
     * @param flag The flag value to set.
     */
    public void setFlag(Flag flag) {
        this.flag = flag;
        toggleSensors(flag);
    }

    /**
     * Get the current fusion characteristics
     * @return The current set flag
     */
    public Flag getFlag(){
        return this.flag;
    }

    /**
     * Start or pause sensors according to the flag.
     * @param flag The flag which defines the current fusion characteristic.
     */
    private void toggleSensors(Flag flag) {
        switch (flag) {
            case RAW:
                accMagSensor.start();
                gyroSensor.pause();
                optFlowSensor.pause();
                break;
            case LOW_PASS:
                accMagSensor.start();
                gyroSensor.pause();
                optFlowSensor.pause();
                break;
            case GYRO:
                accMagSensor.start();
                gyroSensor.start();
                optFlowSensor.pause();
                break;
            case OPT_FLOW:
                accMagSensor.start();
                gyroSensor.start();
                optFlowSensor.start();
                break;
        }
    }

    /**
     * Get an ImageProcessor instance used in {@link de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2}
     * @return the ImageProcessor instance.
     */
    public ImageProcessor getImageProcessor() {
        return (ImageProcessor) optFlowSensor;
    }
}
