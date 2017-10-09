package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Orientation;


/**
 * Created by Tom Kretzschmar on 01.09.2017.
 *
 */

public class OrientationService implements SensorEventListener {


    private static final String TAG = OrientationService.class.getName();

    private static final float ALPHA = 0.25f;
    private List<OrientationListener> listeners;
    private SensorManager sensorManager;

    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];

    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    private Sensor acc;
    private Sensor mag;

    private Orientation orientation;

    public OrientationService(Context context) {
        listeners = new ArrayList<>();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        orientation = new Orientation();
    }

    private Orientation getOrientation() {
        Orientation o = new Orientation();

        // Rotation matrix based on current readings from accelerometer and magnetometer.
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading); // TODO: Remap Coordinate System when turned more then 45° around y or z axis
        // Express the updated rotation matrix as three orientation angles.
        SensorManager.getOrientation(rotationMatrix, orientationAngles);
        o.setX(lowPass((float) Math.toDegrees(orientationAngles[0]), orientation.getX()));
        o.setY(lowPass((float) Math.toDegrees(orientationAngles[1]), orientation.getY()));
        o.setZ(lowPass((float) Math.toDegrees(orientationAngles[2]), orientation.getZ()));
        return o;
    }

    public void registerListener(OrientationListener listener) {
        this.listeners.add(listener);
    }

    public void unregisterListener(OrientationListener listener) {
        listeners.remove(listener);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) accelerometerReading = sensorEvent.values;
        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) magnetometerReading = sensorEvent.values;
        orientation = getOrientation();
        for (OrientationListener listener : listeners) {
            listener.onOrientationChange(orientation);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    public void start() {
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
    * Low-pass filter, which smoothes the newValue values.
    */
    public float lowPass(float newValue, float oldValue) {
        float output = newValue;
        if (oldValue != 0) {
            float diff = newValue - oldValue;
            output = oldValue + (ALPHA * diff);
//            Log.d(TAG, "old:" + oldValue + " new:" + newValue + " out:" + output);
        }
        return output;
    }
}
