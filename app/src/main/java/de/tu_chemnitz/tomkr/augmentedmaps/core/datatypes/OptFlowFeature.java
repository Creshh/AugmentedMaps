package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

/**
 * Created by Tom Kretzschmar on 15.01.2018.<br>
 * <br>
 * A data class holding values for drawing feature points for debug reasons. See {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OptFlowSensor} and {@link de.tu_chemnitz.tomkr.augmentedmaps.view.ARView}<br>
 * Contains current point, old point, and a reliability flag.
 */
public class OptFlowFeature {
    /**
     * Tag for logging
     */
    private static final String TAG = OptFlowFeature.class.getName();
    /**
     * Current point x axis position in pixel.
     */
    private double x;

    /**
     * Current point y axis position in pixel.
     */
    private double y;

    /**
     * Old point x axis position in pixel.
     */
    private double ox;

    /**
     * Old point y axis position in pixel.
     */
    private double oy;

    /**
     * Reliability flag.
     */
    private boolean reliable;

    /**
     * Full constructor.
     * @param x Current point x axis position in pixel.
     * @param y Current point y axis position in pixel.
     * @param ox Old point x axis position in pixel.
     * @param oy Old point y axis position in pixel.
     */
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

    /**
     * @return Reliability flag.
     */
    public boolean isReliable() {
        return reliable;
    }

    /**
     * @param reliable Reliability flag.
     */
    public void setReliable(boolean reliable) {
        this.reliable = reliable;
    }
}
