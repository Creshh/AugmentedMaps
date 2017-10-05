package de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes;

import java.util.Map;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class MapNode {
    private String id;
    private String name;
    private String lat;
    private String lon;
    private float height;
    private String tag;

    public MapNode(String id, String name, String lat, String lon, String tag, float height) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.height = height;
        this.tag = tag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "MapNode{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", lat='" + lat + '\'' + ", lon='" + lon + '\'' + ", height=" + height + ", tag='" + tag + '\'' + '}';
    }
}
