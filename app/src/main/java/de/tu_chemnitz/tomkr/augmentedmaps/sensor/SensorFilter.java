package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.util.Log;

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.LOW_PASS_FACTOR;

/**
 * Created by Tom Kretzschmar on 14.12.2017.
 *
 */

public class SensorFilter {

    private static final String TAG = SensorFilter.class.getName();

    private static final float FUSION_THRESHOLD_LOW_X = 1;
    private static final float FUSION_ADD_FACTOR_X = 0.2f;
    private static final float FUSION_THRESHOLD_HIGH_X = 10f;

    /**
     * Low-pass filter, which smoothes the newValue values.
     */
    public static float lowPass(float newValue, float oldValue) {
        float output = newValue;
        if (oldValue != 0) {
            float diff = newValue - oldValue;
            if(Math.abs(diff) < 180) {
                output = oldValue + (LOW_PASS_FACTOR * diff);
            }
        }
        return output;
    }

    public static Orientation fusion(Orientation sensorOrientation, Orientation oldResultingOrientation, Vec2f motionAngle) {
        Orientation result = new Orientation();
        result.setX(fusionAxis(oldResultingOrientation.getX(), sensorOrientation.getX(), motionAngle.getX()));
        result.setY(fusionAxis(oldResultingOrientation.getY(), sensorOrientation.getY(), motionAngle.getY()));
        return result;
    }

    private static float fusionAxis(float old, float sens, float motion){
        float result;
        float sensDiff = old - sens;
        if(motion < FUSION_THRESHOLD_LOW_X){
            Log.d(TAG, "motion");
            result = old + motion;
        } else if (sensDiff < FUSION_THRESHOLD_LOW_X){
            Log.d(TAG, "sensor1");
            result = sens;
        } else {
            if(Math.abs(sensDiff) > Math.abs(motion)){
                Log.d(TAG, "motion + sensor");
                float diff = sensDiff - motion;
                result = old + motion + (diff*FUSION_ADD_FACTOR_X);
            } else if (Math.abs(motion) < FUSION_THRESHOLD_HIGH_X){
                Log.d(TAG, "sensor + motion");
                float diff = motion - sensDiff;
                result = sens + (diff*FUSION_ADD_FACTOR_X);
            } else {
                Log.d(TAG, "sensor2");
                result = sens;
            }
        }
        return result;
    }
}
