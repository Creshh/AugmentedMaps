package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by Tom Kretzschmar on 21.12.2017.
 * <br>
 * A Sensor implementation which uses the Accelerometer and Magnetometer sensors to get current orientation readings.
 */
public class AccMagSensor implements Sensor, SensorEventListener {
    /**
     * Tag for logging
     */
    private static final String TAG = AccMagSensor.class.getName();


    /**
     * Array for raw accelerometer values
     */
    private float[] accelerometerReading = new float[3];

    /**
     * Array for raw magnetometer values
     */
    private float[] magnetometerReading = new float[3];

    /**
     * The rotation matrix, according to accelerometer and magnetometer readings
     */
    private float[] rotationMatrix = new float[9];

    /**
     * The current orientation values in radians
     */
    private float[] orientationAngles = new float[3];

    /**
     * The current normalized orientation values in degrees
     */
    private float[] rotation;

    /**
     * The accelerometer sensor instance
     */
    private android.hardware.Sensor acc;

    /**
     * The magnetometer sensor instance
     */
    private android.hardware.Sensor mag;

    /**
     * The system sensormanager instance
     */
    private final SensorManager sensorManager;


    /**
     * Full constructor.
     * @param sensorManager The sensorManager instance used to initialize the sensor.
     */
    public AccMagSensor(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        acc = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public float[] getRotation() {
        return rotation;
    }

    @Override
    public void setRotationEstimate(float[] rotation) {
    }

    @Override
    public void start() {
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_FASTEST);
        rotation = new float[3];
    }

    @Override
    public void pause() {
        sensorManager.unregisterListener(this);
    }

    /**
     * Calculate and update current rotation from sensor readings
     */
    private void updateRotation() {
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        float[] remappedMatrix = new float[9];

        SensorManager.remapCoordinateSystem(rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedMatrix); // Correct mapping -> see Using the camera (Y axis along the camera's axis) for an augmented reality application where the rotation angles are neede remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR);

        // Express the updated rotation matrix as three orientation angles.
        SensorManager.getOrientation(remappedMatrix, orientationAngles);
        rotation[0] = (float) (Math.toDegrees(orientationAngles[0]) + 360) % 360;
        rotation[1] = (float) (Math.toDegrees(orientationAngles[1]));
        rotation[2] = (float) (Math.toDegrees(orientationAngles[2]) + 360) % 360;
    }

    /**
     * System sensor callback for retrieving accelerometer and magnetometer readings.
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_ACCELEROMETER) accelerometerReading = sensorEvent.values;
        if (sensorEvent.sensor.getType() == android.hardware.Sensor.TYPE_MAGNETIC_FIELD) magnetometerReading = sensorEvent.values;
        updateRotation();
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {
    }
}
