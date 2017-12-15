package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */
public class DataProcessorA implements DataProcessor {

    private static final String TAG = DataProcessorA.class.getName();
    private float cameraViewAngleH;
    private float cameraViewAngleV;

    @Override
    public void setCameraViewAngleH(float cameraViewAngleH) {
        this.cameraViewAngleH = cameraViewAngleH;
        Log.d(TAG, "FOV=" + cameraViewAngleH);
    }

    @Override
    public void setCameraViewAngleV(float cameraViewAngleV) {
        this.cameraViewAngleV = cameraViewAngleV;
    }

    @Override
    public List<Marker> processData(List<MapNode> nodes, Orientation orientation, Location location) {
        List<Marker> markers = new ArrayList<>();
        for (MapNode node : nodes) {
            markers.add(new Marker(getX(node, orientation, location), getY(node, orientation, location), node.getName() + " [" + node.getLoc().getHeight() + "]"));
        }
        return markers;
    }

    /**
     * Calculate horizontal marker position.
     * @param node MapNode which position should be calculated
     * @param orientation Current device orientation
     * @param location Current device position
     * @return horizontal position of marker in pixel
     */
    private float getX(MapNode node, Orientation orientation, Location location){

        float bearingH = location.getBearingTo(node.getLoc());
        float diffH = bearingH - orientation.getX();
        float offsetH = -1;
        if(Math.abs(diffH) < (cameraViewAngleH/2f)) {
            offsetH = diffH / (cameraViewAngleH/2f); // [-1..1]
            offsetH = ((offsetH + 1) / 2f); // offsetH has to be in Range [0..1] to be drawn
        }
//        Log.d(TAG, node.getName() + " " + orientation + " " + location + " " + diffH + " " + offsetH);
        return offsetH;
    }

    /**
     * Calculate vertical marker position.
     * @param node MapNode which position should be calculated
     * @param orientation Current device orientation
     * @param location Current device position
     * @return vertical position of marker in pixel
     */
    private float getY(MapNode node, Orientation orientation, Location location){
        float betaV = orientation.getY() > 180 ? 360 - orientation.getY() : - orientation.getY();
        float h = location.getHeight() - node.getLoc().getHeight();
        float d = location.getDistanceCorr(node.getLoc());
        float alphaV = (float) Math.toDegrees(Math.atan2(h,d));
        float diffV = betaV + alphaV;
        float offsetV = -1;
        if(Math.abs(diffV) < (cameraViewAngleV/2f)){
            offsetV = diffV / (cameraViewAngleV/2f);
            offsetV = ((offsetV + 1) /2f); // offsetH has to be in Range [0..1] to be drawn
        }
//        Log.d(TAG, node.getName() + " " + orientation + " " + location + " " + diffV + " " + offsetV);
        return offsetV;
    }

}
