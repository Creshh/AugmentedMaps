package de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes;

/**
 * Created by Tom Kretzschmar on 01.09.2017.
 *
 */

public class Location {

    private float lat;
    private float lon;
    private float height;

    public Location(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
        this.height = 0;
    }

    public Location(float lat, float lon, float height) {
        this.lat = lat;
        this.lon = lon;
        this.height = height;
    }

    public Location() {
        this.lat = 0;
        this.lon = 0;
        this.height = 0;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "Location{" + "lat=" + lat + ", lon=" + lon + ", height=" + height + '}';
    }
}
