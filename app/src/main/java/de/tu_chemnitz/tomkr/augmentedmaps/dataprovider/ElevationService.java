package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;

/**
 * Created by Tom Kretzschmar on 06.11.2017.<br>
 * <br>
 * An Interface for Services which acquire altitude data according to given locations.
 */
public interface ElevationService {

    /**
     * Acquires and sets the altitude to the given locations.
     * @param locs The geolocations whithout altitude information.
     * @return The geolocations given with acquired altitude information.
     */
    Location[] getElevation(Location[] locs);

    /**
     * Acquires and sets the altitude to the given location.
     * @param loc The geolocation whithout altitude information.
     * @return The geolocation given with acquired altitude information.
     */
    Location getElevation(Location loc);
}
