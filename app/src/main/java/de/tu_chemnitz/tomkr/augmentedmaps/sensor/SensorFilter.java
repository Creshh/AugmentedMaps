package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.LOW_PASS_FACTOR;

/**
 * Created by Tom Kretzschmar on 14.12.2017.
 *
 */

public class SensorFilter {

    /**
     * IMPORTANT: Don't call multiple times for the same motion value, or result will be wrong. Has to be called each time a new motionValue is obtained from ImageProcessor
     */
    public static float fusion(float newValue, float oldValue, float motionValue, float fov){
        // TODO: calculate angular motion from vector in display coordinates.
        // TODO: or calculate estimated Marker movement, and take it to consideration when updating all markers -> then the marker position must be evaluated, and not the orientation values!!!
        float angle = motionValue * fov; // motionValue is in [0..1]
//        Log.d(TAG, "sensorAngle: " + /*diff*/ 0 + " motionAngle: " + angle + " from value: " + motionValue + " with fov: " + fov);
        return oldValue + angle;
    }

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

}
