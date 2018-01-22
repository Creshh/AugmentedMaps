package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by Tom Kretzschmar on 21.12.2017.<br>
 * <br>
 * A Sensor implementation which uses the Gyroscope sensor to get current rotation readings.<br>
 * Rotation is integrated to get the absolute orientation values resulting from an initial rotation estimate.
 */

public class GyroSensor implements Sensor, SensorEventListener {
    /**
     * Tag for logging
     */
    private static final String TAG = GyroSensor.class.getName();

    /**
     * Constant defining nanoseconds per second
     */
    private static final float NS2S = 1.0f / 1000000000.0f;

    /**
     * Threshold constant which defines the lower bound of rotation values
     */
    private static final float EPSILON = 0.000000001f;

    /**
     * Array holding the current rotation as quaternion
     */
    private final float[] deltaRotationVector = new float[4];

    /**
     * The gyroscope sensor instance
     */
    private final android.hardware.Sensor gyro;

    /**
     * The system sensormanager instance
     */
    private final SensorManager sensorManager;

    /**
     * Last update timestamp to integrate the results
     */
    private float timestamp;

    /**
     * Actual resulting rotation/orientation of the device
     */
    private float[] rotation;

    /**
     * Full constructor.
     * @param sensorManager The sensorManager instance used to initialize the sensor.
     */
    public GyroSensor(SensorManager sensorManager) {
        this.sensorManager = sensorManager;
        gyro = sensorManager.getDefaultSensor(android.hardware.Sensor.TYPE_GYROSCOPE);
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
        sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void pause() {
        sensorManager.unregisterListener(this);
        timestamp = 0;
    }

    /**
     * System sensor callback for retrieving gyroscope readings. Values will be integrated and the current rotation will be updated.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        // This time step's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        if (timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            // Axis of the rotation sample, not normalized yet.
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the time step
            // in order to get a delta rotation from this sample over the time step
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) sin(thetaOverTwo);
            float cosThetaOverTwo = (float) cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
        }
        timestamp = event.timestamp;
        float[] deltaRotationMatrix = new float[9];
//        float[] remappedDeltaMatrix = new float[9];

        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
//        SensorManager.remapCoordinateSystem(deltaRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedDeltaMatrix); // Correct mapping -> see Using the camera (Y axis along the camera's axis) for an augmented reality application where the rotation angles are neede remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR);

        float[] deltaRotation = new float[3];
        SensorManager.getOrientation(deltaRotationMatrix, deltaRotation);
        // concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation and remap coordinates.
        rotation[0] += Math.toDegrees(deltaRotation[1]);
        rotation[1] += Math.toDegrees(deltaRotation[2]);
        rotation[2] += Math.toDegrees(deltaRotation[0]);

//        Log.d(TAG, "deltaRotation: " + deltaRotation[0] + "|" + deltaRotation[1] + "|" + deltaRotation[2]);
    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {
    }
}
