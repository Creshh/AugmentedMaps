package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

import java.util.Locale;

/**
 * Created by Tom Kretzschmar on 01.09.2017.<br>
 * <br>
 * A data class holding orientation values of the device.<br>
 * Contains absolute angular values for all three axis.
 */
public class Orientation {
    /**
     * Tag for logging
     */
    private static final String TAG = Orientation.class.getName();
    /**
     * Angle corresponding to the x axis
     */
    private float x;

    /**
     * Angle corresponding to the y axis
     */
    private float y;

    /**
     * Angle corresponding to the z axis
     */
    private float z;

    /**
     * Default constructor.<br>
     * Sets all values to 0.
     */
    public Orientation() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    /**
     * Full constructor.
     * @param x Angle corresponding to the x axis
     * @param y Angle corresponding to the y axis
     * @param z Angle corresponding to the z axis
     */
    public Orientation(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    /**
     * @return A String representation of the Orientation in the format Orientation{x=0.00,y=0.00,z=0.00}
     */
    @Override
    public String toString() {
        return "Orientation{" +
                "x=" + String.format(Locale.GERMANY,"%.2f", x) +
                ", y=" + String.format(Locale.GERMANY,"%.2f", y) +
                ", z=" + String.format(Locale.GERMANY,"%.2f", z) +
                '}';
    }
}
