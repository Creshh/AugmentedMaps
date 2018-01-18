package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

/**
 * Created by Tom Kretzschmar on 15.01.2018.
 *
 */

public class OptFlowFeature {

    private double x;
    private double y;

    private double ox;
    private double oy;

    private boolean reliable;

    public OptFlowFeature(double x, double y, double ox, double oy) {
        this.x = x;
        this.y = y;
        this.ox = ox;
        this.oy = oy;
        reliable = true;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getOx() {
        return ox;
    }

    public void setOx(double ox) {
        this.ox = ox;
    }

    public double getOy() {
        return oy;
    }

    public void setOy(double oy) {
        this.oy = oy;
    }

    public boolean isReliable() {
        return reliable;
    }

    public void setReliable(boolean reliable) {
        this.reliable = reliable;
    }
}
