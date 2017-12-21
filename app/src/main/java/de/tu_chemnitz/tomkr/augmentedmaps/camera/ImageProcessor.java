package de.tu_chemnitz.tomkr.augmentedmaps.camera;

import android.media.ImageReader;

import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 */

public interface ImageProcessor extends ImageReader.OnImageAvailableListener {
    @Override
    void onImageAvailable(ImageReader imageReader);
}
