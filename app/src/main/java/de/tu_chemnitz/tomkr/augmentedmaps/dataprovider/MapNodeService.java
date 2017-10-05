package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Tag;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public interface MapNodeService {

    List<MapNode> getMapPointsInProximity(Location loc, Tag tags[], int maxDistance);
}
