package de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing;

import java.util.HashMap;


/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public class MotionAnalyzerProvider {

    public enum MotionAnalyzerType {A}
    private static HashMap<MotionAnalyzerType, MotionAnalyzer> registry;

    static{
        registry = new HashMap<>();
    }

    public static MotionAnalyzer getMotionAnalyzer(MotionAnalyzerType type){
        if(!registry.containsKey(type)){
            switch(type){
                case A: registry.put(type, new MotionAnalyzerA());
                    break;
            }
        }
        return registry.get(type);
    }
}