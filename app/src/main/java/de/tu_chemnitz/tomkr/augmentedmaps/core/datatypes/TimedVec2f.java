package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

/**
 * Created by Tom Kretzschmar on 15.01.2018.
 */

public class TimedVec2f extends Vec2f{

    private long timestamp;

    public TimedVec2f(long timestamp, float x, float y) {
        super(x, y);
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
