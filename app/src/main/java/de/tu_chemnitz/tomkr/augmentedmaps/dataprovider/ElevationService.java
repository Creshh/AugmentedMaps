package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Location;

/**
 * Created by Tom Kretzschmar on 06.10.2017.
 *
 */

public interface ElevationService {
    int[] getElevation(Location loc[]);
}
