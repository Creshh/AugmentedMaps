package de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes;

import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public class OutputType {
    private List<Marker> marker;

    public List<Marker> getMarker() {
        return marker;
    }

    public void setMarker(List<Marker> marker) {
        this.marker = marker;
    }
}
