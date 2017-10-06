package de.tu_chemnitz.tomkr.augmentedmaps.processing;



import android.graphics.Point;
import android.support.v7.widget.ThemedSpinnerAdapter;
import android.util.Log;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.InputType;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.OutputType;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;
import de.tu_chemnitz.tomkr.augmentedmaps.view.ARActivity;

import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public class DataProcessorA implements DataProcessor {

    private static final String TAG = DataProcessorA.class.getName();

    private Location loc;
    private float cameraViewAngleH;
    private float cameraViewAngleV;

    @Override
    public void setCameraViewAngleH(float cameraViewAngleH) {
        this.cameraViewAngleH = cameraViewAngleH;
    }

    @Override
    public void setCameraViewAngleV(float cameraViewAngleV) {
        this.cameraViewAngleV = cameraViewAngleV;
    }

    @Override
    public Marker processData(InputType input) {
//        Marker marker = new Marker(x, y, key);
//        Log.d(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++");
//        Log.d(TAG, "Input: " + input.toString());
//        Log.d(TAG, "Loc: " + loc.toString());
//        Log.d(TAG, "+++++++++++++++++++++++++++++++++++++++++++++++++");

        // TODO: calculate Exact Point for Marker

        return new Marker(Helpers.random(1000), Helpers.random(1000), "test");
    }

    @Override
    public void setOwnLocation(Location loc) {
        this.loc = loc;
    }
}
