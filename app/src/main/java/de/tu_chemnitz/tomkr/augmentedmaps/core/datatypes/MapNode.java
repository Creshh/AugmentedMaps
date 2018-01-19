package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

/**
 * Created by Tom Kretzschmar on 21.12.2017.<br>
 * <br>
 * A data class representing a geolocation.<br>
 * Consists of an id, name, {@link Location} and tag.
 */
public class MapNode {
    /**
     * Tag for logging
     */
    private static final String TAG = MapNode.class.getName();

    private String id;
    private String name;
    private Location loc;
    private String tag;

    /**
     * Full constructor.
     * @param id A unique id
     * @param name The name of the geolocation
     * @param loc The {@link Location} of the geolocation
     * @param tag The tag of the geolocation
     */
    public MapNode(String id, String name, Location loc, String tag) {
        this.id = id;
        this.name = name;
        this.loc = loc;
        this.tag = tag;
    }

    /**
     * @return The id of the mapnode.
     */
    public String getId() {
        return id;
    }

    /**
     * @param id The id of the mapnode.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The name of the mapnode
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name of the mapnode.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The location of the mapnode
     */
    public Location getLoc() {
        return loc;
    }

    /**
     * @param loc The location of the mapnode.
     */
    public void setLoc(Location loc) {
        this.loc = loc;
    }

    /**
     * @return The tag of the mapnode
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tag The tag of the mapnode.
     */
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * @return A String representation of the Location in the format MapNode{id='0', name='x', loc=Location{}, tag='x'}
     */
    @Override
    public String toString() {
        return "MapNode{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", loc=" + loc.toString() + ", tag='" + tag + '\'' + '}';
    }
}
