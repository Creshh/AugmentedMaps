package de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes;

import android.media.Image;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Orientation;

/**
 * Created by Tom Kretzschmar on 31.08.2017.
 *
 */

public class InputType {

    private Location loc;
    private Orientation o;
    private Image img;

    public InputType() {
        this.loc = new Location();
        this.o = new Orientation();
    }

    public InputType(Location loc, Orientation o) {
        this.loc = loc;
        this.o = o;
    }

    public InputType(Location loc, Orientation o, Image img) {
        this.loc = loc;
        this.o = o;
        this.img = img;
    }

    public Location getLoc() {
        return loc;
    }

    public void setLoc(Location loc) {
        this.loc = loc;
    }

    public Orientation getO() {
        return o;
    }

    public void setO(Orientation o) {
        this.o = o;
    }

    public Image getImg() {
        return img;
    }

    public void setImg(Image img) {
        this.img = img;
    }
}
