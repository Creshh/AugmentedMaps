package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import java.util.HashMap;


/**
 * Created by Tom Kretzschmar on 21.10.2017.<br>
 * <br>
 * A Factory class which provides an Instance of the given {@link MapNodeService} according to the {@link MapPointServiceType}.<br>
 * Implemented as factory to give the possibility to add new service implementations with ease.
 */
public class MapNodeServiceProvider {

    /**
     * Enum type to adress the different service implementations.
     */
    public enum MapPointServiceType {OVERPASS}

    /**
     * Map of currently available service instances.
     */
    private static HashMap<MapPointServiceType, MapNodeService> registry;

    static{
        registry = new HashMap<>();
    }

    /**
     * Factory method to acquire an MapNodeService of the given type.
     * @param type The implementation to return.
     * @return The instance of {@link MapNodeService}
     */
    public static MapNodeService getMapPointService(MapPointServiceType type){
        if(!registry.containsKey(type)){
            switch(type){
                case OVERPASS: registry.put(type, new OverpassService());
                    break;
            }
        }
        return registry.get(type);
    }
}