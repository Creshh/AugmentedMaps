package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Orientation;

/**
 * Created by Tom Kretzschmar on 11.12.2017.
 * <br>
 * Interface which can be implemented, when an object has to receive callbacks on user device orientation changes from {@link OrientationService}. Has to be registered at the service.
 */
public interface OrientationListener {

    /**
     * Callback method for {@link OrientationService} updates.
     * @param orientation The new orientation of the device.
     */
    void onOrientationChange(Orientation orientation);
}
