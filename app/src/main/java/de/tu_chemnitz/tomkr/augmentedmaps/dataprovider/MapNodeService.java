package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import java.util.List;
import java.util.Map;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MissingParameterException;

/**
 * Created by Tom Kretzschmar on 21.10.2017.<br>
 * <br>
 * An Interface for Services which acquire {@link MapNode} instances in proximity to the user device.
 */

public interface MapNodeService {

    /**
     * Get a list of {@link MapNode} objects which represent geolocations corresponding to the given tags.
     * @param loc Current user device location.
     * @param tags Map of tags, which define the type of the Nodes which should be returned.
     * @param maxDistance Maximum distance to the user device the points should have
     * @return A list of geolocations corresponding to the given attributes
     * @throws MissingParameterException An exception if parameters are missing or not suitable for the implementation.
     */
    List<MapNode> getMapPointsInProximity(Location loc, Map<String, List<String>> tags, int maxDistance) throws MissingParameterException;
}
