package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public interface LocationListener {
    void onLocationChange(Location loc);
}
