package de.tu_chemnitz.tomkr.augmentedmaps.view;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadFactory;

import de.tu_chemnitz.tomkr.augmentedmaps.R;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.camera.PermissionHandler;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.InputType;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.OutputType;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.MapNodeService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.MapNodeServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessorProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationService;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;

import static android.R.attr.data;
import static android.R.attr.y;
import static android.R.string.no;

/**
 * Created by Tom Kretzschmar on 21.09.2017.
 *
 */

public class ARActivity extends Activity implements OrientationListener, LocationListener {

    private static final String TAG = ARActivity.class.getName();

    private Camera2 camera;
    private OrientationService orientationService;
    private MapNodeService mapNodeService;
    private DataProcessor dataProcessor;
    private TextView orientationView;
    private TextView locationView;
    private TextureView textureView;
    private ARView arView;
    private Thread debugHelperThread;
    private boolean stop = false;

    private Location ownLocation;
    private List<MapNode> mapNodes;

    private LocationService locationService;
    private Orientation orientation;
    private List<MarkerDrawable> markerList;

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

        markerList = Helpers.createSampleMarker(4, 1920, 1080);
        arView.setMarkerListRef(markerList);

        locationView = (TextView) findViewById(R.id.pos);
        locationService = new LocationService(this);

        mapNodeService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
        dataProcessor = DataProcessorProvider.getDataProcessor(DataProcessorProvider.DataProcessorType.A);
        dataProcessor.setCameraViewAngleH(100);
        dataProcessor.setCameraViewAngleH(60);
    }

    @Override
    public void onResume() {
        super.onResume();
        orientationService.registerListener(this);
        orientationService.start();

        camera.startService();
        stop = false;
//        debugHelperThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (!stop) {
//                    Log.d(TAG, "" + arView.getWidth() + "___" + arView.getHeight());
//                    try {
//                        Thread.sleep(5 * 1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        debugHelperThread.start();
        locationService.start();
        locationService.registerListener(this);
        locationService.pushLocation();

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!stop){
                    if(mapNodes!= null && mapNodes.size() > 0){
                        markerList = new ArrayList<>();
                        for(MapNode node : mapNodes){
                            Marker marker = dataProcessor.processData(new InputType(node.getLoc(), orientation));
                            markerList.add(new MarkerDrawable(marker));
                        }
                        arView.setMarkerListRef(markerList);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                arView.invalidate();
                            }
                        });

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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
        locationService.unregisterListener(this);
        locationService.stop();
        super.onPause();
    }


    @Override
    public void onOrientationChange(Orientation values) {
//        Log.d("ORIENTATION", "X" + values.toString());
        orientationView.setText(values.toString());
        orientation = values;
    }

    private void hideSystemUI() {
        // Set the IMMERSIVE flag. Set the content to appear under the system bars so that the content doesn't resize when the system bars hide and show.
        textureView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }


    private void acquireOwnHeight(final Location loc) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ElevationService es = ElevationServiceProvider.getElevationService(ElevationServiceProvider.ElevationServiceType.OPEN_ELEVATION);
                int elevations[] = es.getElevation(new Location[]{loc});
                for (int e : elevations) {
                    Log.d(TAG, "Elevation -> " + e);
                }
                loc.setHeight(elevations[0]);
            }
        });
        t.start();
        while(t.isAlive()){
            try {
                Thread.sleep(100);
                Log.d(TAG, "sleep");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
//        locationView.setText("Pos|Height -> " + loc);
    }

    private void acquireMapNodes(final Location loc){
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mapNodes = mapNodeService.getMapPointsInProximity(loc, null, 5000);
            }
        });
        t.start();
    }

    @Override
    public void onBigLocationChange(Location loc) {
        acquireOwnHeight(loc);
        dataProcessor.setOwnLocation(loc);
        Log.d(TAG, "BigLoc -> " + loc);
        locationView.setText("Pos|BigLoc -> " + loc);
        acquireMapNodes(loc);
    }

    @Override
    public void onSmallLocationChange(Location loc) {
        acquireOwnHeight(loc);
        dataProcessor.setOwnLocation(loc);
        Log.d(TAG, "SmallLoc -> " + loc);
        locationView.setText("Pos|SmallLoc -> " + loc);
    }

    @Override
    public void onInitialLocation(Location loc) {
        acquireOwnHeight(loc);
        dataProcessor.setOwnLocation(loc);
        Log.d(TAG, "InitialLoc -> " + loc);
        locationView.setText("Pos|IniLoc -> " + loc);
        acquireMapNodes(loc);
    }
}