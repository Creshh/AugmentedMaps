package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import java.util.HashMap;

/**
 * Created by Tom Kretzschmar on 06.11.2017.<br>
 * <br>
 * A Factory class which provides an Instance of the given {@link ElevationService} according to the {@link ElevationServiceType}.<br>
 * Implemented as factory to give the possibility to add new service implementations with ease.
 */
public class ElevationServiceProvider {

    /**
     * Enum type to adress the different service implementations.
     */
    public enum ElevationServiceType {OPEN_ELEVATION}

    /**
     * Map of currently available service instances.
     */
    private static HashMap<ElevationServiceType, ElevationService> registry;

    static{
        registry = new HashMap<>();
    }

    /**
     * Factory method to acquire an ElevationService of the given type.
     * @param type The implementation to return.
     * @return The instance of {@link ElevationService}
     */
    public static ElevationService getElevationService(ElevationServiceType type){
        if(!registry.containsKey(type)){
            switch(type){
                case OPEN_ELEVATION: registry.put(type, new OpenElevationService());
                    break;
            }
        }
        return registry.get(type);
    }
}
