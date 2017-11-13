package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public interface DataProcessor {
    void setCameraViewAngleH(float cameraViewAngleH);

    void setCameraViewAngleV(float cameraViewAngleV);

    Marker processData(MapNode node, Orientation orientation, Location location);
}
