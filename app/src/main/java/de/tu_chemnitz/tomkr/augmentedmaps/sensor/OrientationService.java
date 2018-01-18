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
 * Created by Tom Kretzschmar on 21.12.2017.
 */

public class OrientationService extends LooperThread {


    public enum Flag {RAW, LOW_PASS, GYRO, OPT_FLOW}

    private Flag flag;
    private boolean init;

    private static final String TAG = OrientationService.class.getName();
    private List<OrientationListener> listeners;
    private Sensor accMagSensor;
    private Sensor gyroSensor;
    private Sensor optFlowSensor;

    private float[] rotation;

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

    private void notifyListeners(Orientation orientation) {
        for (OrientationListener listener : listeners) {
            listener.onOrientationChange(orientation);
        }
    }

    public void registerListener(OrientationListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterListener(OrientationListener listener) {
        listeners.remove(listener);
    }

    public void setFlag(Flag flag) {
        this.flag = flag;
        toggleSensors(flag);
    }

    public Flag getFlag(){
        return this.flag;
    }

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

    public ImageProcessor getImageProcessor() {
        return (ImageProcessor) optFlowSensor;
    }

}
