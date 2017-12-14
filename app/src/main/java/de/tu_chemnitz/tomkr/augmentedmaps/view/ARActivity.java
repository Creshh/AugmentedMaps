package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;
import de.tu_chemnitz.tomkr.augmentedmaps.util.PermissionHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.core.Controller;

import static de.tu_chemnitz.tomkr.augmentedmaps.R.id.preview;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_FPS_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_LOC_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_ORIENTATION_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_STATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_VIEW;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class ARActivity extends Activity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener{

    private static final String TAG = ARActivity.class.getName();

    private Camera2 camera;
    private TextView orientationView;
    private TextView locationView;
    private TextView stateView;
    private TextView fpsView;
    private TextureView textureView;
    private ARView arView;
    private CheckBox toggleLowPass;
    private CheckBox toggleMotion;
    private Button logBtn;

    private static ARView arViewStatic;

    private Controller controller;

    public static ARView getView() {
        return arViewStatic;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        PermissionHandler p = new PermissionHandler(this);
        p.checkPermission();
        Helpers.dir = getFilesDir();
        textureView = (TextureView) findViewById(preview);
        arView = (ARView) findViewById(R.id.arview);
        arViewStatic = arView;
        orientationView = (TextView) findViewById(R.id.orientation);
        locationView = (TextView) findViewById(R.id.pos);
        stateView = (TextView) findViewById(R.id.state);
        fpsView = (TextView) findViewById(R.id.fpsView);
        toggleLowPass = (CheckBox) findViewById(R.id.toggleLP);
        toggleLowPass.setOnCheckedChangeListener(this);
        toggleMotion = (CheckBox) findViewById(R.id.toggleBA);
        toggleMotion.setOnCheckedChangeListener(this);
        logBtn = (Button) findViewById(R.id.btnLog);
        logBtn.setOnClickListener(this);

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
                if (message.what == MSG_UPDATE_STATE_VIEW) {
                    stateView.setText((String) message.obj);
                }
                if (message.what == MSG_UPDATE_FPS_VIEW) {
                    fpsView.setText((String) message.obj);
                }
                return true;
            }
        };
        
        controller = new Controller(updateViewCallback, this, camera);
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

    @Override
    public void onCheckedChanged(CompoundButton box, boolean b) {
        switch (box.getId()){
            case R.id.toggleLP:
                Log.d(TAG, "toggle LowPass");
                controller.lowPass = b;
                break;
            case R.id.toggleBA:
                Log.d(TAG, "toogle ImageProcessor");
                controller.motion = b;
                break;
        }
    }

    @Override
    public void onClick(View view) {
        controller.logToFile();
    }
}
