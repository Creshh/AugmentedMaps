package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.TriggerEvent;
import android.hardware.TriggerEventListener;
import android.util.Log;

import java.util.Arrays;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * Created by Tom Kretzschmar on 21.12.2017.<br>
 * <br>
 * A Sensor implementation which uses the Gyroscope sensor to get current rotation readings.<br>
 * Rotation is integrated to get the absolute orientation values resulting from an initial rotation estimate.
 */

public class GeoMagSensor implements iSensor, SensorEventListener {
	/**
	 * Tag for logging
	 */
	private static final String TAG = GeoMagSensor.class.getName();

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
	private final TriggerEventListener triggerEventListener;

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
	 *
	 * @param sensorManager The sensorManager instance used to initialize the sensor.
	 */
	public GeoMagSensor(SensorManager sensorManager) {
		this.sensorManager = sensorManager;
		gyro = sensorManager.getDefaultSensor(17);
//      2019-10-02 18:18:12.620 32057-32057/de.tu_chemnitz.tomkr.augmentedmaps I/System.out: {Sensor name="ACCELEROMETER", vendor="MTK", version=1, type=1, maxRange=39.2266, resolution=0.0012, power=0.001, minDelay=10000}
//      2019-10-02 18:18:12.621 32057-32057/de.tu_chemnitz.tomkr.augmentedmaps I/System.out: {Sensor name="MAGNETOMETER", vendor="MTK", version=1, type=2, maxRange=4912.0, resolution=0.15, power=0.001, minDelay=20000}
//      2019-10-02 18:18:12.622 32057-32057/de.tu_chemnitz.tomkr.augmentedmaps I/System.out: {Sensor name="ORIENTATION", vendor="MTK", version=1, type=3, maxRange=360.0, resolution=0.00390625, power=0.001, minDelay=20000}
//      2019-10-02 18:18:12.623 32057-32057/de.tu_chemnitz.tomkr.augmentedmaps I/System.out: {Sensor name="LIGHT", vendor="MTK", version=1, type=5, maxRange=65535.0, resolution=1.0, power=0.001, minDelay=0}
//      2019-10-02 18:18:12.624 32057-32057/de.tu_chemnitz.tomkr.augmentedmaps I/System.out: {Sensor name="PROXIMITY", vendor="MTK", version=1, type=8, maxRange=1.0, resolution=1.0, power=0.001, minDelay=0}
//      2019-10-02 18:18:12.625 32057-32057/de.tu_chemnitz.tomkr.augmentedmaps I/System.out: {Sensor name="significant motion detector", vendor="Sony Mobile Inc", version=104, type=17, maxRange=1.0, resolution=1.0, power=0.5, minDelay=-1}
//      2019-10-02 18:18:12.625 32057-32057/de.tu_chemnitz.tomkr.augmentedmaps I/System.out: {Sensor name="GeoMag Rotation Vector Sensor", vendor="AOSP", version=3, type=20, maxRange=1.0, resolution=5.9604645E-8, power=0.002, minDelay=10000}
//      2019-10-02 18:18:12.627 32057-32057/de.tu_chemnitz.tomkr.augmentedmaps E/GyroSensor: NO GYROSCOPE SENSOR !
//	https://source.android.com/devices/sensors/sensor-types
//      Note: When there is no gyroscope on the device (and only when there is no gyroscope),
//      you may implement the rotation vector, linear acceleration, and gravity sensors without using the gyroscope.
//		so use Geomagnetic rotation vector Low power sensor
// 		Accelerometer, Magnetometer, MUST NOT USE Gyroscope
		if (gyro != null) {
			Log.e(TAG, "NO GYROSCOPE SENSOR !");
			Log.e(TAG, "USING SENSOR_TYPE_GEOMAGNETIC_ROTATION_VECTOR (Type 17) !");
//			E/SensorManager: Trigger Sensors should use the requestTriggerSensor.
			Log.e(TAG, "REPORTING MODE: " + gyro.getReportingMode());
//REPORTING_MODE_CONTINUOUS			0
//REPORTING_MODE_ON_CHANGE			1
//REPORTING_MODE_ONE_SHOT			2
//REPORTING_MODE_SPECIAL_TRIGGER	3
		}
		triggerEventListener = new TriggerEventListener() {
			@Override
			public void onTrigger(TriggerEvent event) {

				float initialGeo = event.values[0];
				Log.e(TAG, "initial geo: " + Arrays.toString(event.values));
			}
		};
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
		boolean triggered = sensorManager.requestTriggerSensor(triggerEventListener, gyro);
//		sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	public void pause() {
		sensorManager.cancelTriggerSensor(triggerEventListener, gyro);
//		sensorManager.unregisterListener(this);
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
//sensors_event_t.data[0] = rot_axis.x*sin(theta/2)
//sensors_event_t.data[1] = rot_axis.y*sin(theta/2)
//sensors_event_t.data[2] = rot_axis.z*sin(theta/2)
//sensors_event_t.data[3] = cos(theta/2)
//sensors_event_t.data[4] = estimated_accuracy (in radians)
//The heading error must be less than estimated_accuracy 95% of the time.
// This sensor must use a gyroscope as the main orientation change input.

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
