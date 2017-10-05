package de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public class Marker {

    private float x;
    private float y;
    private String key;

    public Marker(){}

    public Marker(float x, float y, String key) {
        this.x = x;
        this.y = y;
        this.key = key;
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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
