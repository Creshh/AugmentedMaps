package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import java.util.HashMap;


/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */
public class MapNodeServiceProvider {

    public enum MapPointServiceType {OVERPASS}
    private static HashMap<MapPointServiceType, MapNodeService> registry;

    static{
        registry = new HashMap<>();
    }

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