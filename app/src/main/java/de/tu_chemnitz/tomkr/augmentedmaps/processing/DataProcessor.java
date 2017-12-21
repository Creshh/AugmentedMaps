package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public interface DataProcessor {
    List<Marker> processData(List<MapNode> nodes, Orientation orientation, Location location);
}
