package de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class MapNode {
    private String id;
    private String name;
    private Location loc;
    private String tag;

    public MapNode(String id, String name, Location loc, String tag) {
        this.id = id;
        this.name = name;
        this.loc = loc;
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

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "MapNode{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", loc=" + loc.toString() + ", tag='" + tag + '\'' + '}';
    }
}
