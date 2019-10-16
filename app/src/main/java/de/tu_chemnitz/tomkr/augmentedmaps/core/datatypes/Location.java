package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

import androidx.annotation.NonNull;

/**
 * Created by Tom Kretzschmar on 01.11.2017.<br>
 * <br>
 * A data class for holding a location value.<br>
 * The position consists of latitude, longitude and altitude.<br>
 * The class provides different methods for distance or heading calculations.<br>
 */

public class Location {
    /**
     * Tag for logging
     */
    private static final String TAG = Location.class.getName();

    /**
     * Latitude of location.
     */
    private float lat;

    /**
     * Longitude of location.
     */
    private float lon;

    /**
     * Altitude of location in meter above sea level.
     */
    private int alt;

    /**
     * Radius of earth.
     */
    private static final float R = 6371e3f;

    /**
     * Standard constructor which sets alitude to -1.
     *
     * @param lat Latitude of location.
     * @param lon Longitude of location.
     */
    public Location(float lat, float lon) {
        this.lat = lat;
        this.lon = lon;
        this.alt = -1;
    }

    /**
     * Full constructor.
     *
     * @param lat Latitude
     * @param lon Longitude
     * @param alt Altitude in meters.
     */
    public Location(float lat, float lon, int alt) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
    }

    /**
     * Default constructor, which sets all values to -1.
     */
    public Location() {
        this.lat = -1;
        this.lon = -1;
        this.alt = -1;
    }

    /**
     * @return Latitude of given location.
     */
    public float getLat() {
        return lat;
    }

    /**
     * @param lat Latitude of given location.
     */
    public void setLat(float lat) {
        this.lat = lat;
    }

    /**
     * @return Longitude of given location.
     */
    public float getLon() {
        return lon;
    }

    /**
     * @param lon Longitude of given location.
     */
    public void setLon(float lon) {
        this.lon = lon;
    }

    /**
     * @return Altitude of given location.
     */
    public int getAlt() {
        return alt;
    }

    /**
     * @param alt Altitude of given location.
     */
    public void setAlt(int alt) {
        this.alt = alt;
    }

    /**
     * Calculate the shortest distance in meteres between the current and the given location loc.<br>
     * The calculation uses the haversine formula. Therefore the values can be seen as correct.
     *
     * @param loc The second location, the distance will be measured to.
     * @return The distance between the two locations in meter.
     */
    public float getDistanceCorr(Location loc) {
        float phi1 = (float) Math.toRadians(lat);
        float phi2 = (float) Math.toRadians(loc.getLat());
        float dphi = (float) Math.toRadians(loc.getLat() - lat);
        float dld = (float) Math.toRadians(loc.getLon() - lon);

        float a = (float) (Math.sin(dphi / 2) * Math.sin(dphi / 2) + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dld / 2) * Math.sin(dld / 2));
        float c = (float) (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));
        return R * c;
    }

    /**
     * Calculate the shortest distance in meteres between the current and the given location loc.<br>
     * The calculation uses the equirectangular projection for calculating the distance. Therefore the values are approximated and can differ much from the correct values.
     *
     * @param loc The second location, the distance will be measured to.
     * @return The distance between the two locations in meter.
     */
    public float getDistanceApprx(Location loc) {
        float phi1 = (float) Math.toRadians(lat);
        float phi2 = (float) Math.toRadians(loc.getLat());
        float ld1 = (float) Math.toRadians(lon);
        float ld2 = (float) Math.toRadians(loc.getLon());

        float x = (float) ((phi2 - phi1) * Math.cos((ld1 + ld2) / 2));
        float y = ld2 - ld1;
        return (float) Math.sqrt(x * x + y * y) * R;
    }

    /**
     * Calculate the initial bearing to the given location.
     *
     * @param loc The second location, the bearing will be calculated to.
     * @return The bearing angle relative to the line of longitude.
     */
    public float getBearingTo(Location loc) {
        float phi1 = (float) Math.toRadians(lat);
        float phi2 = (float) Math.toRadians(loc.getLat());
        float ld1 = (float) Math.toRadians(lon);
        float ld2 = (float) Math.toRadians(loc.getLon());

        float y = (float) (Math.sin(ld2 - ld1) * Math.cos(phi2));
        float x = (float) (Math.cos(phi1) * Math.sin(phi2) - Math.sin(phi1) * Math.cos(phi2) * Math.cos(ld2 - ld1));
        return (float) (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    /**
     * @return A String representation of the Location in the format Location{lat=0, lon=0, alt=0}
     */
    @Override
    @NonNull
    public String toString() {
        return "Location{" + "lat=" + lat + ", lon=" + lon + ", alt=" + alt + '}';
    }
}
