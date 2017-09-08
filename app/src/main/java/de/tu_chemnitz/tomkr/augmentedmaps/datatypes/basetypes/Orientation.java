package de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes;

/**
 * Created by Tom Kretzschmar on 01.09.2017.
 *
 */

public class Orientation {

    private float x;
    private float y;
    private float z;

    public Orientation() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

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
}
