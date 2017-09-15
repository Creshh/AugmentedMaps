package de.tu_chemnitz.tomkr.augmentedmaps.testframework.groundtruth;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.testframework.InputTypeGT;

import static java.lang.Float.parseFloat;

/**
 * Created by Tom Kretzschmar on 03.09.2017.
 *
 */

public class GTService {

    private static final String FIELD_DELIM = ";";
    private static final String VALUE_DELIM = ":";
    private static final String GT_DELIM = "|";
    private static final String GT_FILE = "res\\gt.dat";


    public static void addSample(InputTypeGT sample){
        StringBuilder sb = new StringBuilder();
        String imgKey = String.valueOf(sample.getImg().getTimestamp());
        //ImageHelpers.writeImage(imgKey, sample.getImg());
        sb.append("img").append(VALUE_DELIM).append(imgKey).append(FIELD_DELIM);
        sb.append("lat").append(VALUE_DELIM).append(sample.getLoc().getLat()).append(FIELD_DELIM);
        sb.append("lon").append(VALUE_DELIM).append(sample.getLoc().getLon()).append(FIELD_DELIM);
        sb.append("x").append(VALUE_DELIM).append(sample.getO().getX()).append(FIELD_DELIM);
        sb.append("y").append(VALUE_DELIM).append(sample.getO().getY()).append(FIELD_DELIM);
        sb.append("z").append(VALUE_DELIM).append(sample.getO().getZ()).append(FIELD_DELIM);
        sb.append("GT").append(VALUE_DELIM);
        for(int i = 0; i < sample.getGtMarker().size(); i++){
            Marker m = sample.getGtMarker().get(i);
            sb.append(m.getX()).append(",").append(m.getY());
            if(i != sample.getGtMarker().size()-1) sb.append(GT_DELIM);
        }
    }

    public static List<InputTypeGT> getSamples(){
        List<InputTypeGT> samples = new ArrayList<>();

        // Read Samples from file and put into List samples.
        try(BufferedReader br = new BufferedReader(new FileReader(GT_FILE))) {
            for(String line; (line = br.readLine()) != null; ) {
                samples.add(parseGTSample(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return samples;
    }

    /**
     * Parse Ground Truth string sample which must be for example in the following format:
     * img:s01;lat:50.43;lon:30.84;x:30;y:20;z:60;GT:key,0.5,0.56|key,0.23,0.89|key,0.10,0.11
     *
     * @param sampleString GT String to parse
     * @return InputTypeGT with parsed values
     */
    private static InputTypeGT parseGTSample(String sampleString){
        InputTypeGT sample = new InputTypeGT();
        for(String token : sampleString.split(FIELD_DELIM)){
            String key = token.split(VALUE_DELIM)[0];
            String value = token.split(VALUE_DELIM)[1];
            switch (key){
                case "img":
                    //sample.setImg(ImageHelpers.readImage(value));
                    break;
                case "lat":
                    sample.getLoc().setLat(parseFloat(value));
                    break;
                case "lon":
                    sample.getLoc().setLon(parseFloat(value));
                    break;
                case "x":
                    sample.getO().setX(parseFloat(value));
                    break;
                case "y":
                    sample.getO().setY(parseFloat(value));
                    break;
                case "z":
                    sample.getO().setZ(parseFloat(value));
                    break;
                case "GT":
                    for(String m : value.split(GT_DELIM)){
                        String gtKey = m.split(",")[0];
                        float x = parseFloat(m.split(",")[1]);
                        float y = parseFloat(m.split(",")[2]);
                        sample.getGtMarker().put(gtKey, new Marker(x,y,gtKey));
                    }
                    break;
            }
        }
        return sample;
    }
}
