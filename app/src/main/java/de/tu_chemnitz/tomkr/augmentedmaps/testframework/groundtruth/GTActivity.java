package de.tu_chemnitz.tomkr.augmentedmaps.testframework.groundtruth;

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
import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2Listener;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.ImageSaver;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.PermissionHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;


public class GTActivity extends Activity implements View.OnClickListener, OrientationListener, Camera2Listener {
    private static final String TAG = GTActivity.class.getName();

    private Camera2 camera;
    private OrientationService orientationService;
    private TextView orientationView;
    private TextureView textureView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gt);
        textureView = (TextureView) findViewById(R.id.preview);
        hideSystemUI();

        Button btn = (Button) findViewById(R.id.captureBtn);
        btn.setOnClickListener(this);

        orientationView = (TextView) findViewById(R.id.orientation);

        orientationService = new OrientationService(this);


        PermissionHandler pm = new PermissionHandler(this);
        pm.checkPermission();
        camera = new Camera2(textureView, this, getWindowManager().getDefaultDisplay());
        camera.registerImageAvailableListener(onImageAvailableListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationService.registerListener(this);
        orientationService.start();

        camera.startService();
    }

    @Override
    public void onPause() {
        orientationService.unregisterListener(this);
        orientationService.stop();

        camera.stopService();
        super.onPause();
    }


    @Override
    public void onClick(View view) {
        camera.takePicture();
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

    /**
     * This a callback object for the {@link ImageReader}. "onImageAvailable" will be called when a
     * still image is ready to be saved.
     */
    private final ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.d(TAG, "IMAGE AVAILABLE");
            String filename = "image_" + Helpers.createTimeStamp(System.currentTimeMillis()) + ".jpg";

//            showToast("Saved: " + filename);
            Thread t = new Thread(new ImageSaver(reader.acquireLatestImage()));
            t.start();
        }
    };
}