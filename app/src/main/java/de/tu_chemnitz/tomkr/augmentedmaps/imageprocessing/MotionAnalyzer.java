package de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing;

import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;

import de.tu_chemnitz.tomkr.augmentedmaps.core.OpenCVHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public interface MotionAnalyzer extends ImageReader.OnImageAvailableListener {

    Vec2f getRelativeMotionVector(OpenCVHandler openCVHandler);


    @Override
    void onImageAvailable(ImageReader imageReader);
}
