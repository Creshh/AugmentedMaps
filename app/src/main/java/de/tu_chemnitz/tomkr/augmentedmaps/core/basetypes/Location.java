package de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes;

/**
 * Created by Tom Kretzschmar on 01.09.2017.
 *
 */

public class Location {

    private float lat;
    private float lon;
    private int height;

    private static final float R = 6371e3f;

    public Location(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
        this.height = -1;
    }

    public Location(float lat, float lon, int height) {
        this.lat = lat;
        this.lon = lon;
        this.height = height;
    }

    public Location() {
        this.lat = 0;
        this.lon = 0;
        this.height = -1;
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

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getDistanceCorr(Location loc) {
        float phi1 = (float) Math.toRadians(lat);
        float phi2 = (float) Math.toRadians(loc.getLat());
        float dphi = (float) Math.toRadians(loc.getLat() - lat);
        float dld = (float) Math.toRadians(loc.getLon() - lon);

        float a = (float) (Math.sin(dphi / 2) * Math.sin(dphi / 2) + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dld / 2) * Math.sin(dld / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        return R * c;
    }

    public float getDistanceApprx(Location loc) {
        float phi1 = (float) Math.toRadians(lat);
        float phi2 = (float) Math.toRadians(loc.getLat());
        float ld1 = (float) Math.toRadians(lon);
        float ld2 = (float) Math.toRadians(loc.getLon());

        float x = (float) ((phi2 - phi1) * Math.cos((ld1 + ld2) / 2));
        float y = ld2 - ld1;
        return (float) Math.sqrt(x * x + y * y) * R;
    }

    public float getBearingTo(Location loc) {
        float phi1 = (float) Math.toRadians(lat);
        float phi2 = (float) Math.toRadians(loc.getLat());
        float ld1 = (float) Math.toRadians(lon);
        float ld2 = (float) Math.toRadians(loc.getLon());

        float y = (float) (Math.sin(ld2 - ld1) * Math.cos(phi2));
        float x = (float) (Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(ld2 - ld1));
        return (float) (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    @Override
    public String toString() {
        return "Location{" + "lat=" + lat + ", lon=" + lon + ", height=" + height + '}';
    }
}
