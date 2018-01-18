package de.tu_chemnitz.tomkr.augmentedmaps.location;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public interface LocationListener {
    void onLocationChange(Location loc);
}
