package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

/**
 * Created by Tom Kretzschmar on 21.12.2017.<br>
 * <br>
 * An Interface for sensor services which provide orientation information about the user device.
 */
public interface Sensor {

    /**
     * Get the currently measured rotation of the user device.
     * @return The measured rotation/orientation vector.
     */
    float[] getRotation();

    /**
     * Set the (estimated) currently best known orientation as origin for further orientation updates by this service. Used to accuse for drift or noise.
     * @param rotation The estimated rotation/orientation vector.
     */
    void setRotationEstimate(float[] rotation);

    /**
     * (Re)start the service and the continous updates.
     */
    void start();

    /**
     * Pause the service and the continous updates.
     */
    void pause();
}