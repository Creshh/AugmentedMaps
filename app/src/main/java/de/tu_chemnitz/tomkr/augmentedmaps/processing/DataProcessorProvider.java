package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import java.util.HashMap;

/**
 * Created by Tom Kretzschmar on 31.10.2017.
 * <br>
 * A Factory class which provides an Instance of the given {@link DataProcessor} according to the {@link DataProcessorType}.<br>
 * Implemented as factory to give the possibility to add new service implementations with ease.
 */
public class DataProcessorProvider {

    /**
     * Enum type to adress the different service implementations.
     */
    public enum DataProcessorType {A}

    /**
     * Map of currently available service instances.
     */
    private static HashMap<DataProcessorType, DataProcessor> registry;

    static{
        registry = new HashMap<>();
    }

    /**
     * Factory method to acquire an DataProcessor of the given type.
     * @param type The implementation to return.
     * @return The instance of {@link DataProcessor}
     */
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
