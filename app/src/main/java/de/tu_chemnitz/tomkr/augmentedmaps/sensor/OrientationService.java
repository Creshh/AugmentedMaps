package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.LOW_PASS_FACTOR;


/**
 * Created by Tom Kretzschmar on 01.09.2017.
 *
 */

public class OrientationService implements SensorEventListener {

    private static final String TAG = OrientationService.class.getName();
    private List<OrientationListener> listeners;
    private SensorManager sensorManager;

    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];

    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    private Sensor acc;
    private Sensor mag;

    private Orientation orientation;
//    private int defaultDisplayRotation;

    public OrientationService(Context context) {
        listeners = new ArrayList<>();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        orientation = new Orientation();
//        defaultDisplayRotation = ((WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
    }

    private Orientation getOrientation() {
        Orientation o = new Orientation();

        // Rotation matrix based on current readings from accelerometer and magnetometer.
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);

        // Remap Coordinate System correctly
        float[] remappedMatrix = new float[9];

//        if (defaultDisplayRotation == 1) {
        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedMatrix); // Correct mapping -> see Using the camera (Y axis along the camera's axis) for an augmented reality application where the rotation angles are neede remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR);
//        } else {
//            SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_Z, remappedMatrix);
//        }

        // Express the updated rotation matrix as three orientation angles.
        SensorManager.getOrientation(remappedMatrix, orientationAngles);
        o.setX((float) (Math.toDegrees(orientationAngles[0]) + 360) % 360);
        o.setY((float) (Math.toDegrees(orientationAngles[1]) + 360) % 360);
        o.setZ((float) (Math.toDegrees(orientationAngles[2]) + 360) % 360);
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
}
