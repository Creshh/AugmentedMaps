package de.tu_chemnitz.tomkr.augmentedmaps.testframework;

import java.util.ArrayList;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.OutputType;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.InputTypeGT;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessorProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.testframework.groundtruth.GTService;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public class Evaluator {

    private List<InputTypeGT> samples;
    private List<OutputType> outputs;

    public Evaluator(){
        samples = GTService.getSamples();
    }

    public void evaluateDataProcessors(List<DataProcessorProvider.DataProcessorType> types){
//        List<DataProcessor> dataProcessors = new ArrayList<>();
//        for(DataProcessorProvider.DataProcessorType type : types){
//            dataProcessors.add(DataProcessorProvider.getDataProcessor(type));
//        }
//
//        for(DataProcessor dataProcessor : dataProcessors){
//            for(InputTypeGT sample : samples){
//                Marker marker = dataProcessor.processData(sample);
//                outputs.add(marker);
//                calculatePrecision(sample, marker);
//            }
//        }
    }

    private void calculatePrecision(InputTypeGT input, OutputType output){
//        // compare output with Ground Truth for every Marker
//        // maybe use special key to match marker from output to input (OSM title, lat+long, hash, ... ?)
//        for(Marker mO : output.getMarker()){
//            if(input.getGtMarker().containsKey(mO.getKey())){
//                log("Key not in  Input");
//            } else {
//                float xOut = mO.getX();
//                float yOut = mO.getY();
//                Marker mI = input.getGtMarker().get(mO.getKey());
//                float xIn = mI.getX();
//                float yIn = mI.getY();
//
//                float xDiff = xOut - xIn;
//                float yDiff = yOut - yIn;
//
//                float diff = (float) Math.sqrt((xDiff*xDiff) + (yDiff*yDiff));
//                log("Key: " + mO.getKey() + " -> diff: " + diff);
//            }
//
//        }
    }

    private void log(String line){

    }


}
