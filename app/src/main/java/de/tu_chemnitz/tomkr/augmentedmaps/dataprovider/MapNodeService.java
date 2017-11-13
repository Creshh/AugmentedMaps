package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import java.util.List;
import java.util.Map;

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.MissingParameterException;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public interface MapNodeService {

    List<MapNode> getMapPointsInProximity(Location loc, Map<String, List<String>> tags, int maxDistance) throws MissingParameterException;
}
