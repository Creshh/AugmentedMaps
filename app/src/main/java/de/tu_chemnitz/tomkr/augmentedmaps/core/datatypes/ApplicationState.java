package de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes;

/**
 * Created by Tom Kretzschmar on 13.11.2017.<br>
 * <br>
 * An enum which defines all possible states of the whole application. See {@link de.tu_chemnitz.tomkr.augmentedmaps.core.Controller}.
 */
public enum ApplicationState {
    /**
     * State after the startup of the application.
     */
    INITIALIZED,

    /**
     * State after acquiring the current location of the user device.
     */
    LOCATION_ACQUIRED,

    /**
     * State after acquiring the current altitude of the user device.
     */
    OWN_ALTITUDE_ACQUIRED,

    /**
     * State after fetching all mapnodes around the current position.
     */
    NODES_ACQUIRED,

    /**
     * State after fetching all corresponding altitude values.
     */
    NODES_ALTITUDE_ACQUIRED,

    /**
     * State while continously processing and displaying the available data.
     */
    DATA_PROCESSING
}
