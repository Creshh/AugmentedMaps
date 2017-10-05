package de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes;

import android.media.Image;

import java.util.HashMap;
import java.util.Map;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public class InputTypeGT extends InputType {

    private Map<String,Marker> gtMarker;

    public InputTypeGT(){}

    public InputTypeGT(Location loc, Orientation o) {
        super(loc, o);
        this.gtMarker = new HashMap<>();
    }

    public InputTypeGT(Location loc, Orientation o, Image img) {
        super(loc, o, img);
        this.gtMarker = gtMarker;
    }

    public Map<String,Marker> getGtMarker() {
        return gtMarker;
    }
}
