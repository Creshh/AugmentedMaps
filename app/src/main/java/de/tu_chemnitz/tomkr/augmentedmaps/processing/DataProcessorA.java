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

import static android.R.attr.offset;
import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public class DataProcessorA implements DataProcessor {

    //private static final String TAG = DataProcessorA.class.getName();
    private static final String TAG = "H_POS";
    private Location loc;
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
    public Marker processData(InputType input) {
        // calculate horizontal position
        float bearingH = this.loc.getBearingTo(input.getLoc());
        float diffH = bearingH - input.getO().getX();
        float offsetH = -1;
        if(Math.abs(diffH) < (cameraViewAngleH/2f)) {
            offsetH = diffH / (cameraViewAngleH/2f); // [-1..1]
            offsetH = ((offsetH + 1) / 2f); // offsetH has to be in Range [0..1] to be drawn
        }

        // TODO: calculate vertical position
        float betaV = input.getO().getY() > 180 ? 360 - input.getO().getY() : -input.getO().getY();
        float h = loc.getHeight() - input.getLoc().getHeight();
        float d = loc.getDistanceCorr(input.getLoc());
        float alphaV = (float) Math.toDegrees(Math.atan2(h,d));
        float diffV = betaV + alphaV;
        float offsetV = -1;
        if(Math.abs(betaV) < (cameraViewAngleV/2f)){ // TODO -> diffV?????
            offsetV = diffV / (cameraViewAngleV/2f);
            offsetV = ((offsetV + 1) /2f);
        } // TODO offsetV  is always -1


//        Log.d(TAG, "bearing: " + bearingH + "| orientation: " + input.getO().getX() + " | diff: " + diffH + " | offset: " + offsetH);
        return new Marker(offsetH, offsetV, "test");
//        return new Marker(offsetH, .8f, "test");
    }

    @Override
    public void setOwnLocation(Location loc) {
        this.loc = loc;
    }

}
