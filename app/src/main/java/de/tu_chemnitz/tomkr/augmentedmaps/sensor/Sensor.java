package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

/**
 * Created by Tom Kretzschmar on 21.12.2017.
 *
 */

public interface Sensor {

    float[] getRotation();

    void setRotationEstimate(float[] rotation);

    void start();

    void pause();
}
