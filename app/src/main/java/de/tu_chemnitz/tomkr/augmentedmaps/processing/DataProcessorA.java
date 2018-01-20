package de.tu_chemnitz.tomkr.augmentedmaps.processing;

import java.util.ArrayList;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Orientation;

/**
 * Created by Tom Kretzschmar on 31.10.2017.<br>
 * <br>
 * A {@link DataProcessor} which maps {@link MapNode} to {@link Marker} objects.
 */
public class DataProcessorA implements DataProcessor {
    /**
     * Tag for logging
     */
    private static final String TAG = DataProcessorA.class.getName();

    /**
     * Processes the given data to map the points to ar.
     * @param nodes The MapNodes to map.
     * @param orientation The current orienation of the device.
     * @param location The current location of the device.
     * @return A List of Marker representing the MapNodes in the ar level.
     */
    @Override
    public List<Marker> processData(List<MapNode> nodes, Orientation orientation, Location location) {
        List<Marker> markers = new ArrayList<>();
        for (MapNode node : nodes) {
            markers.add(new Marker(getX(node, orientation, location), getY(node, orientation, location), node.getName() + " [" + node.getLoc().getAlt() + "]"));
        }
        return markers;
    }

    /**
     * Calculate horizontal marker position.
     * @param node MapNode which position should be calculated
     * @param orientation Current device orientation
     * @param location Current device position
     * @return horizontal position of marker in the interval [0..1]
     */
    private float getX(MapNode node, Orientation orientation, Location location){

        float bearingH = location.getBearingTo(node.getLoc());
        float diffH = bearingH - orientation.getX();
        float offsetH = -1;
        if(Math.abs(diffH) < (Camera2.fov[0]/2f)) {
            offsetH = diffH / (Camera2.fov[1]/2f); // [-1..1]
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
     * @return vertical position of marker in the interval [0..1]
     */
    private float getY(MapNode node, Orientation orientation, Location location){
        float betaV = orientation.getY() > 180 ? 360 - orientation.getY() : - orientation.getY();
        float h = location.getAlt() - node.getLoc().getAlt();
        float d = location.getDistanceCorr(node.getLoc());
        float alphaV = (float) Math.toDegrees(Math.atan2(h,d));
        float diffV = betaV + alphaV;
        float offsetV = -1;
        if(Math.abs(diffV) < (Camera2.fov[0]/2f)){
            offsetV = diffV / (Camera2.fov[1]/2f);
            offsetV = ((offsetV + 1) /2f); // offsetH has to be in Range [0..1] to be drawn
        }
//        Log.d(TAG, node.getName() + " " + orientation + " " + location + " " + diffV + " " + offsetV);
        return offsetV;
    }

}
