package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Orientation;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public interface DataProcessor {
    List<Marker> processData(List<MapNode> nodes, Orientation orientation, Location location);
}
