package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

/**
 * Created by Tom Kretzschmar on 05.11.2017.<br>
 * <br>
 * A data class representing a vector with two elements of type float.
 */

public class Vec2f {
    /**
     * Tag for logging
     */
    private static final String TAG = Vec2f.class.getName();

    /**
     * x value
     */
    private float x;
    /**
     * y value
     */
    private float y;

    /**
     * Full constructor.
     * @param x the x value of the vector
     * @param y the y value of the vector
     */
    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
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

    /**
     * Substract the given vector vec from this vector.
     * @param vec the second vector which should be substracted from the called one.
     */
    public void substract(Vec2f vec) {
        this.x = x - vec.getX();
        this.y = y - vec.getY();
    }
}
