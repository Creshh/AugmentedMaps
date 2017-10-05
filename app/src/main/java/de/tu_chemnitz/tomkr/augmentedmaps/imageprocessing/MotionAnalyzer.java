package de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing;

import android.media.Image;

import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public interface MotionAnalyzer {

    public Vec2f getRelativeMotionVector(Image prev, Image cur);


}
