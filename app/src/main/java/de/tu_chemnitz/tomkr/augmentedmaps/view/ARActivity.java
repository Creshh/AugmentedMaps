package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.PermissionHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationService;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;

import static android.R.attr.y;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class ARActivity extends Activity implements OrientationListener, LocationListener{

    private static final String TAG = ARActivity.class.getName();

    private Camera2 camera;
    private OrientationService orientationService;
    private TextView orientationView;
    private TextView locationView;
    private TextureView textureView;
    private ARView arView;
    private Thread debugHelperThread;
    private boolean stop = false;

    private LocationService locationService;

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

        locationView = (TextView) findViewById(R.id.pos);
        locationService = new LocationService(this);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ElevationService es = ElevationServiceProvider.getElevationService(ElevationServiceProvider.ElevationServiceType.OPEN_ELEVATION);
                int elevations[] = es.getElevation(new Location[]{new Location(50.821428f, 12.945283f), new Location(50.9234237f, 13.0326581f)});
                for(int e : elevations){
                    Log.d(TAG, "Elevation -> " + e);
                }
            }
        });
        t.start();

    }

    @Override
    public void onResume() {
        super.onResume();
        orientationService.registerListener(this);
        orientationService.start();

        camera.startService();
        stop = false;
        debugHelperThread = new Thread(new Runnable() {
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
        debugHelperThread.start();
        locationService.start();
        locationService.registerListener(this);
        locationService.pushLocation();
    }

    @Override
    public void onPause() {
        orientationService.unregisterListener(this);
        orientationService.stop();

        camera.stopService();
        stop = true;
        locationService.unregisterListener(this);
        locationService.stop();
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


    @Override
    public void onBigLocationChange(Location loc) {
        Log.d(TAG, "BigLoc -> " + loc);
        locationView.setText("Pos|BigLoc -> " + loc);
    }

    @Override
    public void onSmallLocationChange(Location loc) {
        Log.d(TAG, "SmallLoc -> " + loc);
        locationView.setText("Pos|SmallLoc -> " + loc);
    }

    @Override
    public void onInitialLocation(Location loc) {
        Log.d(TAG, "InitialLoc -> " + loc);
        locationView.setText("Pos|IniLoc -> " + loc);
    }
}
