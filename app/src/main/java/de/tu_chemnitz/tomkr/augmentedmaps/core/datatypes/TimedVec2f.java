package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

/**
 * Created by Tom Kretzschmar on 15.01.2018.<br>
 * <br>
 * A data class for logging.<br>
 * Extends {@link Vec2f} with a timestamp.
 */

public class TimedVec2f extends Vec2f{
    /**
     * Tag for logging
     */
    private static final String TAG = TimedVec2f.class.getName();

    /**
     * Timestamp in milliseconds.
     */
    private long timestamp;

    /**
     * Full constructor.
     * @param timestamp Timestamp in milliseconds.
     * @param x x value of vector.
     * @param y y value of vector.
     */
    public TimedVec2f(long timestamp, float x, float y) {
        super(x, y);
        this.timestamp = timestamp;
    }

    /**
     * @return Timestamp in milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp Timestamp in milliseconds.
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
