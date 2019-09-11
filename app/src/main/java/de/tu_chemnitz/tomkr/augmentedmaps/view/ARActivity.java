package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.TextView;


import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;
import de.tu_chemnitz.tomkr.augmentedmaps.util.PermissionHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.core.Controller;

import static de.tu_chemnitz.tomkr.augmentedmaps.R.id.preview;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_FPS_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_INFO_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_LOC_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_ORIENTATION_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_STATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_VIEW;

/**
 * Created by Tom Kretzschmar on 21.10.2017.<br>
 * <br>
 * The main activity for this augmented reality application. Holds all view objects and delegates system callbacks.<br>
 * Implements the main thread handler callback to receive view update messages.
 */
public class ARActivity extends Activity implements View.OnClickListener {
    /**
     * Tag for logging
     */
    private static final String TAG = ARActivity.class.getName();

    /**
     * A Camera2 implementation for displaying the camera preview and contolling its state.
     */
    private Camera2 camera;

    /**
     * A TextView to display the current device orientation. For testing/debug purpose.
     */
    private TextView orientationView;

    /**
     * A TextView to display the current device location. For testing/debug purpose.
     */
    private TextView locationView;

    /**
     * A TextView to display the current {@link de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.ApplicationState}. For testing/debug purpose.
     */
    private TextView stateView;

    /**
     * A TextView to display the current fps of data processing. For testing/debug purpose.
     */
    private TextView fpsView;

    /**
     * A TextView to display other currently available information. For testing/debug purpose.
     */
    private TextView infoView;

    /**
     * The target for the camera preview.
     */
    private TextureView textureView;

    /**
     * The augmented reality view layer which displays {@link de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Marker}.
     */
    private ARView arView;

    /**
     * Static reference to the ar view for debug reasons.
     */
    private static ARView arViewStatic;

    /**
     * The instance of the main application controller.
     */
    private Controller controller;

    /**
     * Conveniance method for testing/debug reasons.<br>
     * Used to set the OptFlowFeatures out of {@link de.tu_chemnitz.tomkr.augmentedmaps.sensor.OptFlowSensor}.
     * @return The {@link ARView} object bound to this actitvity.
     */
    public static ARView getView() {
        return arViewStatic;
    }

    /**
     * Inititalize the main Activity. System callback.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        PermissionHandler p = new PermissionHandler(this);
        p.checkPermissions();
        textureView = (TextureView) findViewById(preview);
        arView = (ARView) findViewById(R.id.arview);
        arViewStatic = arView;
        orientationView = (TextView) findViewById(R.id.orientation);
        locationView = (TextView) findViewById(R.id.pos);
        stateView = (TextView) findViewById(R.id.state);
        fpsView = (TextView) findViewById(R.id.fpsView);
        infoView = (TextView) findViewById(R.id.infoView);

        RadioButton toggleLowPass = (RadioButton) findViewById(R.id.toggleLowPass);
        RadioButton toggleOptFlow = (RadioButton) findViewById(R.id.toggleOptFlow);
        RadioButton toggleRaw = (RadioButton) findViewById(R.id.toggleRaw);
        RadioButton toggleGyro = (RadioButton) findViewById(R.id.toggleGyro);
        toggleLowPass.setOnClickListener(this);
        toggleOptFlow.setOnClickListener(this);
        toggleRaw.setOnClickListener(this);
        toggleGyro.setOnClickListener(this);
        toggleRaw.setChecked(true);

        findViewById(R.id.btnLog).setOnClickListener(this);

        camera = new Camera2(textureView, this, getWindowManager().getDefaultDisplay());

        Handler.Callback updateViewCallback = new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message.what == MSG_UPDATE_VIEW) {
                    arView.setMarkerList(controller.getMarkerList());
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
                if (message.what == MSG_UPDATE_INFO_VIEW) {
                    infoView.setText((String) message.obj);
                }
                return true;
            }
        };

        controller = new Controller(updateViewCallback, this, camera);
    }

    /**
     * System callback. Will be delegated to camera and controller instances.
     */
    @Override
    public void onResume() {
        super.onResume();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUI();
        camera.startService();
        controller.startApplication();
    }

    /**
     * System callback. Will be delegated to camera and controller instances.
     */
    @Override
    public void onPause() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        camera.stopService();
        controller.stopApplication();
        super.onPause();
    }

    /**
     * System callback. Will be delegated to camera and controller instances.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        controller.quitApplication();
    }

    /**
     * Helper function used to hide the system bars.
     */
    private void hideSystemUI() {
        // Set the IMMERSIVE flag. Set the content to appear under the system bars so that the content doesn't resize when the system bars hide and show.
        textureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    /**
     * OnClick listener for debug RadioButtons in ARView. Used to change mode of {@link OrientationService}.
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toggleRaw:
                Log.d(TAG, "toggle Raw");
                controller.setOrientationFlag(OrientationService.Flag.RAW);
                break;
            case R.id.toggleLowPass:
                Log.d(TAG, "toggle LowPass");
                controller.setOrientationFlag(OrientationService.Flag.LOW_PASS);
                break;
            case R.id.toggleGyro:
                Log.d(TAG, "toggle Gyro");
                controller.setOrientationFlag(OrientationService.Flag.GYRO);
                break;

            case R.id.toggleOptFlow:
                Log.d(TAG, "toogle OpticalFlow");
                controller.setOrientationFlag(OrientationService.Flag.OPT_FLOW);
                break;
            case R.id.btnLog:
                controller.logToFile();
                break;
        }
    }
}
