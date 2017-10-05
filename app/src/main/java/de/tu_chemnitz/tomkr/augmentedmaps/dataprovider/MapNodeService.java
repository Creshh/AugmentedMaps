package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes.Tag;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public interface MapNodeService {

    List<MapNode> getMapPointsInProximity(Location loc, Tag tags[], int maxDistance);
}
