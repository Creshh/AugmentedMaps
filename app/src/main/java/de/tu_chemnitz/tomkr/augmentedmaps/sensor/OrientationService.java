package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.content.Context;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;


import de.tu_chemnitz.tomkr.augmentedmaps.core.LooperThread;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.ImageProcessor;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.TARGET_FRAMETIME;

/**
 * Created by Tom Kretzschmar on 21.12.2017.
 *
 */

public class OrientationService extends LooperThread {

    public static final float GYRO_FAC = 0.80f;
    public static final float ACCMAG_FAC = 0.2f;
    public static final float OPTFLOW_FAC = 0.18f;

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
    }


    @Override
    protected void loop() {
        float[] accMag;
        float[] gyro;
        float[] optFlow;

        if (init) {
            rotation = accMagSensor.getRotation();
            if (rotation != null) {
                init = false;
                gyroSensor.setRotationEstimate(rotation);
                optFlowSensor.setRotationEstimate(rotation);
            }
        } else {
            switch (flag) {
                case RAW:
                    accMag = accMagSensor.getRotation();
                    rotation = accMag;
                    break;
                case LOW_PASS:
                    accMag = accMagSensor.getRotation();
                    for (int i = 0; i < 3; i++) {
                        rotation[i] = (rotation[i] * (1 - ACCMAG_FAC)) + (accMag[i] * ACCMAG_FAC);
                    }
                    break;
                case GYRO:
                    gyro = gyroSensor.getRotation();
                    accMag = accMagSensor.getRotation();
                    for (int i = 0; i < 3; i++) {
                        rotation[i] = (gyro[i] * (GYRO_FAC + OPTFLOW_FAC)) + (accMag[i] * ACCMAG_FAC);
                    }
                    break;
                case OPT_FLOW:
                    optFlow = optFlowSensor.getRotation();
                    gyro = gyroSensor.getRotation();
                    accMag = accMagSensor.getRotation();
                    for (int i = 0; i < 3; i++) {
                        rotation[i] = (gyro[i] * GYRO_FAC) + (accMag[i] * ACCMAG_FAC) + (optFlow[i] * OPTFLOW_FAC);
                    }
                    break;
            }
            gyroSensor.setRotationEstimate(rotation);
            optFlowSensor.setRotationEstimate(rotation);
            notifyListeners(new Orientation(rotation[0], rotation[1], rotation[2]));
        }
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
    }

    public ImageProcessor getImageProcessor(){
        return (ImageProcessor) optFlowSensor;
    }

}
