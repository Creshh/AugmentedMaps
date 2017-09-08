package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import java.util.HashMap;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public class DataProcessorProvider {

    public enum DataProcessorType {A}
    private static HashMap<DataProcessorType, DataProcessor> registry;

    static{
        registry = new HashMap<>();
    }

    public static DataProcessor getDataProcessor(DataProcessorType type){
        if(!registry.containsKey(type)){
            switch(type){
                case A: registry.put(type, new DataProcessorA());
                        break;
            }
        }
        return registry.get(type);
    }
}
