package de.tu_chemnitz.tomkr.augmentedmaps.dataprovider;

import java.util.HashMap;

/**
 * Created by Tom Kretzschmar on 06.10.2017.
 *
 */

public class ElevationServiceProvider {

    public enum ElevationServiceType {OPEN_ELEVATION}
    private static HashMap<ElevationServiceType, ElevationService> registry;

    static{
        registry = new HashMap<>();
    }

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
