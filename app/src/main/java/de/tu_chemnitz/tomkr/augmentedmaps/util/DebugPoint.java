package de.tu_chemnitz.tomkr.augmentedmaps.util;

/**
 * Created by Tom Kretzschmar on 15.01.2018.
 *
 */

public class DebugPoint {

    public DebugPoint(double x, double y, double ox, double oy) {
        this.x = x;
        this.y = y;
        this.ox = ox;
        this.oy = oy;
        reliable = true;
    }

    public double x;
    public double y;

    public double ox;
    public double oy;

    public boolean reliable;
}
