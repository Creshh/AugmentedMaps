package de.tu_chemnitz.tomkr.augmentedmaps.location;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;

/**
 * Created by Tom Kretzschmar on 05.10.2017.<br>
 * <br>
 * Interface which can be implemented, when an object has to receive callbacks on user device location changes from {@link LocationService}. Has to be registered at the service.
 */

public interface LocationListener {

    /**
     * Callback method if {@link LocationService} registeres a change in device location.
     * @param loc The new location of the device.
     */
    void onLocationChange(Location loc);
}
