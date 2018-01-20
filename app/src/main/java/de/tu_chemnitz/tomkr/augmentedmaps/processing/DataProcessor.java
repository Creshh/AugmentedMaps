package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Orientation;

/**
 * Created by Tom Kretzschmar on 31.10.2017.<br>
 * <br>
 * An Interface for Services which process {@link MapNode} and device information to map them to the augmented reality level.
 */
public interface DataProcessor {

    /**
     * Processes the given data to map the points to ar.
     * @param nodes The MapNodes to map.
     * @param orientation The current orienation of the device.
     * @param location The current location of the device.
     * @return A List of Marker representing the MapNodes in the ar level.
     */
    List<Marker> processData(List<MapNode> nodes, Orientation orientation, Location location);
}
