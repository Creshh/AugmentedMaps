package de.tu_chemnitz.tomkr.augmentedmaps.util;

/**
 * Created by Tom Kretzschmar on 15.01.2018.
 *
 */

public class LogPoint {

    public long timestamp;
    public Vec2f point;

    public LogPoint(long timestamp, Vec2f point) {
        this.timestamp = timestamp;
        this.point = point;
    }
}
