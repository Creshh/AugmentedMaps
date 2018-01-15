package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by Tom Kretzschmar on 21.12.2017.
 *
 */

public class GyroSensor implements Sensor, SensorEventListener{
    private static final String TAG = GyroSensor.class.getName();

    private static final float NS2S = 1.0f / 1000000000.0f;
    private static final float EPSILON = 0.000000001f;
    private final float[] deltaRotationVector = new float[4];
    private final android.hardware.Sensor gyro;
    private final SensorManager sensorManager;
    private float timestamp;
    private float[] rotation;

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
            float omegaMagnitude = (float) sqrt(axisX*axisX + axisY*axisY + axisZ*axisZ);

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
        // in order to get the updated rotation and remap Coordinates.
        rotation[0] += Math.toDegrees(deltaRotation[1]);
        rotation[1] += Math.toDegrees(deltaRotation[2]);
        rotation[2] += Math.toDegrees(deltaRotation[0]);

//        for(int i = 0; i<3; i++){
//            rotation[i] += Math.toDegrees(deltaRotation[i]);
//        }
        Log.d(TAG, "deltaRotation: " + deltaRotation[0] + "|" + deltaRotation[1] + "|" + deltaRotation[2]);
    }

//    @Override
//    public void onSensorChanged(SensorEvent event) {
//        if(rotation != null) {
//            float[] gyroMatrix = getRotationMatrixFromOrientation(rotation);
//            // This timestep's delta rotation to be multiplied by the current rotation
//            // after computing it from the gyro sample data.
//            if (timestamp != 0) {
//                final float dT = (event.timestamp - timestamp) * NS2S;
//                timestamp = event.timestamp;
//                // Axis of the rotation sample, not normalized yet.
//                float axisX = event.values[0];
//                float axisY = event.values[1];
//                float axisZ = event.values[2];
//
//                // Calculate the angular speed of the sample
//                float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
//
//                // Normalize the rotation vector if it's big enough to get the axis
//                // (that is, EPSILON should represent your maximum allowable margin of error)
//                if (omegaMagnitude > EPSILON) {
//                    axisX /= omegaMagnitude;
//                    axisY /= omegaMagnitude;
//                    axisZ /= omegaMagnitude;
//                }
//
//                // Integrate around this axis with the angular speed by the timestep
//                // in order to get a delta rotation from this sample over the timestep
//                // We will convert this axis-angle representation of the delta rotation
//                // into a quaternion before turning it into the rotation matrix.
//                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
//                float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
//                float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
//                deltaRotationVector[0] = sinThetaOverTwo * axisX;
//                deltaRotationVector[1] = sinThetaOverTwo * axisY;
//                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
//                deltaRotationVector[3] = cosThetaOverTwo;
//
//                float[] deltaRotationMatrix = new float[9];
////                float[] remappedDeltaMatrix = new float[9];
//                SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
////                SensorManager.remapCoordinateSystem(deltaRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedDeltaMatrix); // Correct mapping -> see Using the camera (Y axis along the camera's axis) for an augmented reality application where the rotation angles are neede remapCoordinateSystem(inR, AXIS_X, AXIS_Z, outR);
//
//                // apply the new rotation interval on the gyroscope based rotation matrix
//                gyroMatrix = matrixMultiplication(gyroMatrix, deltaRotationMatrix);
//
//                // get the gyroscope based orientation from the rotation matrix
//                float[] deltaRotation = new float[3];
//
//                SensorManager.getOrientation(gyroMatrix, deltaRotation);
//
////                rotation[0] = (float) Math.toDegrees(deltaRotation[0]);
////                rotation[1] = (float) Math.toDegrees(deltaRotation[1]);
////                rotation[2] = (float) Math.toDegrees(deltaRotation[2]);
//
//                rotation[0] = deltaRotation[0];
//                rotation[1] = deltaRotation[1];
//                rotation[2] = deltaRotation[2];
////                rotation[0] = rotation[0] + deltaRotation[0];
////                rotation[1] = rotation[1] + deltaRotation[1];
////                rotation[2] = rotation[2] + deltaRotation[2];
////                rotation[0] = (float) (rotation[0] + Math.toDegrees(deltaRotation[0]));
////                rotation[1] = (float) (rotation[1] + Math.toDegrees(deltaRotation[1]));
////                rotation[2] = (float) (rotation[2] + Math.toDegrees(deltaRotation[2]));
//
//            } else {
//                timestamp = event.timestamp;
//            }
//            Log.d(TAG, "rotation: " + rotation[0] + "|" + rotation[1] + "|" + rotation[2]);
//        }
//    }

    @Override
    public void onAccuracyChanged(android.hardware.Sensor sensor, int i) {}


    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float) sin(o[1]);
        float cosX = (float) cos(o[1]);
        float sinY = (float) sin(o[2]);
        float cosY = (float) cos(o[2]);
        float sinZ = (float) sin(o[0]);
        float cosZ = (float) cos(o[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }
}
