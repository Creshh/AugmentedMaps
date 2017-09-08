package de.tu_chemnitz.tomkr.augmentedmaps.camera;

import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by Tom Kretzschmar on 03.09.2017.
 *
 */

public class ImageHelpers {



    public static Image readImage(String imgKey){
        final File file = new File("res\\" + imgKey + "\\.jpeg");

        return null;
    }

    public static void writeImage(String imgKey, final Image image) {
        final File file = new File("res\\" + imgKey + "\\.jpeg");

        Runnable r = new Runnable() {
            @Override
            public void run() {
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.remaining()];
                buffer.get(bytes);
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(file);
                    output.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    image.close();
                    if (null != output) {
                        try {
                            output.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        r.run();
    }
}
