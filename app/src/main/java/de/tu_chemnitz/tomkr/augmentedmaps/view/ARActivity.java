package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.app.Activity;
import android.media.ImageReader;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.ImageSaver;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.PermissionHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;
import de.tu_chemnitz.tomkr.augmentedmaps.testframework.groundtruth.GTActivity;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class ARActivity extends Activity implements OrientationListener{

    private static final String TAG = ARActivity.class.getName();

    private Camera2 camera;
    private OrientationService orientationService;
    private TextView orientationView;
    private TextureView textureView;
    private ARView arView;
    private Thread t;
    private boolean stop = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        textureView = (TextureView) findViewById(R.id.preview);
        arView = (ARView) findViewById(R.id.arview);
        hideSystemUI();

        orientationView = (TextView) findViewById(R.id.orientation);
        orientationService = new OrientationService(this);

        PermissionHandler pm = new PermissionHandler(this);
        pm.checkPermission();
        camera = Camera2.instantiate(textureView, this, getWindowManager().getDefaultDisplay());


        arView.setMarkerListRef(Helpers.createSampleMarker(4, 1920, 1080));


    }

    @Override
    public void onResume() {
        super.onResume();
        orientationService.registerListener(this);
        orientationService.start();

        camera.startService();
        stop = false;
        t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!stop){
                    Log.d(TAG, "" + arView.getWidth() + "___" + arView.getHeight());
                    try {
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    @Override
    public void onPause() {
        orientationService.unregisterListener(this);
        orientationService.stop();

        camera.stopService();
        stop = true;
        super.onPause();
    }


    @Override
    public void onOrientationChange(Orientation values) {
//        Log.d("ORIENTATION", "X" + values.toString());
        orientationView.setText(values.toString());
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag. Set the content to appear under the system bars so that the content doesn't resize when the system bars hide and show.
        textureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }


}
