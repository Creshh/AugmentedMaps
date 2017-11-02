package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.InputType;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public interface DataProcessor {
    void setCameraViewAngleH(float cameraViewAngleH);

    void setCameraViewAngleV(float cameraViewAngleV);

    Marker processData(InputType input);

    void setOwnLocation(Location loc);
}
