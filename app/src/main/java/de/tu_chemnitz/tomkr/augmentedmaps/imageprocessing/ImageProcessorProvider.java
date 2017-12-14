package de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing;

import android.util.Log;

import org.opencv.android.OpenCVLoader;

import java.util.HashMap;


/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public class ImageProcessorProvider {

    private static final String TAG = ImageProcessorProvider.class.getName();

    public enum MotionAnalyzerType {OPTICAL_FLOW, HORIZON}
    private static HashMap<MotionAnalyzerType, ImageProcessor> registry;

    static{
        registry = new HashMap<>();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV not loaded");
            System.exit(1);
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    public static ImageProcessor getMotionAnalyzer(MotionAnalyzerType type){
        if(!registry.containsKey(type)){
            switch(type){
                case OPTICAL_FLOW: registry.put(type, new OpticalFlow());
                    break;
                case HORIZON: registry.put(type, new HorizonTracker());
            }
        }
        return registry.get(type);
    }
}