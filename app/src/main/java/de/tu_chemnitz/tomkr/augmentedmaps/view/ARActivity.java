package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;


import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.PermissionHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.core.Controller;

import static de.tu_chemnitz.tomkr.augmentedmaps.R.id.preview;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_LOC_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_ORIENTATION_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_VIEW;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class ARActivity extends Activity{

    private static final String TAG = ARActivity.class.getName();

    private Camera2 camera;
    private TextView orientationView;
    private TextView locationView;
    private TextureView textureView;
    private ARView arView;

    private Controller controller;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        PermissionHandler p = new PermissionHandler(this);
        p.checkPermission();
        textureView = (TextureView) findViewById(preview);
        arView = (ARView) findViewById(R.id.arview);
        orientationView = (TextView) findViewById(R.id.orientation);
        locationView = (TextView) findViewById(R.id.pos);
        camera = new Camera2(textureView, this, getWindowManager().getDefaultDisplay());

        Handler.Callback updateViewCallback = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == MSG_UPDATE_VIEW) {
                    arView.setMarkerListRef(controller.getMarkerList());
                    arView.invalidate();
                }
                if (message.what == MSG_UPDATE_ORIENTATION_VIEW) {
                    orientationView.setText((String) message.obj);
                }
                if (message.what == MSG_UPDATE_LOC_VIEW) {
                    locationView.setText((String) message.obj);
                }
                return true;
            }
        };
        
        controller = new Controller(updateViewCallback, this);
        controller.setFov(camera.calculateFOV());
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();
        camera.startService();
        controller.startApplication();
    }

    @Override
    public void onPause() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        camera.stopService();
        controller.stopApplication();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.quitApplication();
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag. Set the content to appear under the system bars so that the content doesn't resize when the system bars hide and show.
        textureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }
}
