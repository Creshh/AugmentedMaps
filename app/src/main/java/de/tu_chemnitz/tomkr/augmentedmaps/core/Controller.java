package de.tu_chemnitz.tomkr.augmentedmaps.core;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.MissingParameterException;
import de.tu_chemnitz.tomkr.augmentedmaps.core.basetypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.core.complextypes.InputType;
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
import de.tu_chemnitz.tomkr.augmentedmaps.view.MarkerDrawable;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.DIST_THRESHOLD;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MAX_DISTANCE;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_PROCESS_DATA;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_LOC_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_MAPNODES;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_NODE_HEIGHT;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_ORIENTATION_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_OWN_HEIGHT;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.TARGET_FRAMETIME;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 * Main Application Controller
 */
public class Controller extends Thread implements OrientationListener, LocationListener, Handler.Callback {

    private static final String TAG = Controller.class.getName();

    private enum State{
        INITIALIZED, LOCATION_ACQUIRED, OWN_HEIGHT_ACQUIRED, NODES_ACQUIRED, NODES_HEIGHT_ACQUIRED, DATA_PROCESSING
    }
    private State state;
    private boolean smallLocationUpdate;

    private final LocationService locationService;
    private final OrientationService orientationService;
    private final DataProcessor dataProcessor;
    private final ElevationService elevationService;
    private final MapNodeService mapNodeService;

    private Orientation orientation;
    private Location loc;

    private Map<String, List<String>> tags;
    private List<MapNode> mapNodes;
    private List<MarkerDrawable> markerList;

    private HandlerThread dataFetchThread;
    private Handler dataFetchHandler;
    private HandlerThread dataProcThread;
    private Handler dataProcHandler;
    private Handler mainHandler;

    private boolean stop;
    private boolean pause;
    private final Object pauseLock;

    public Controller(Handler.Callback activityCallback, Context context) {
        tags = Helpers.getTagsFromConfig(context);
        mainHandler = new Handler(Looper.getMainLooper(), activityCallback);
        mapNodeService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
        dataProcessor = DataProcessorProvider.getDataProcessor(DataProcessorProvider.DataProcessorType.A);
        elevationService = ElevationServiceProvider.getElevationService(ElevationServiceProvider.ElevationServiceType.OPEN_ELEVATION);
        locationService = new LocationService(context);
        orientationService = new OrientationService(context);

        state = State.INITIALIZED;
        pauseLock = new Object();
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
        while (!stop) {
            long starttime = System.currentTimeMillis();

            synchronized (pauseLock) {
                while (pause) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            switch(state){
                case INITIALIZED:
                    // location will be acquired automatically or call pushLocation now
                    locationService.pushLocation();
                    break;
                case LOCATION_ACQUIRED:
                    dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_OWN_HEIGHT));
                    break;
                case OWN_HEIGHT_ACQUIRED:
                    if(smallLocationUpdate) {
                        smallLocationUpdate = false;
                        state = State.DATA_PROCESSING;
                    }
                    else {
                        dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_MAPNODES));
                    }
                    break;
                case NODES_ACQUIRED:
                    dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_NODE_HEIGHT));
                    break;
                case NODES_HEIGHT_ACQUIRED:
                case DATA_PROCESSING:
                    dataProcHandler.sendMessage(dataProcHandler.obtainMessage(MSG_PROCESS_DATA));
                    break;
            }
            mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_VIEW));

            long frametime = (System.currentTimeMillis()-starttime);
            if(frametime < TARGET_FRAMETIME){
                try {
                    sleep(TARGET_FRAMETIME-frametime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startApplication() {
        stop = false;
        synchronized (pauseLock){
            pause = false;
            pauseLock.notifyAll();
        }

        smallLocationUpdate = false;

        dataFetchThread = new HandlerThread("DataFetchThread");
        dataFetchThread.start();
        dataFetchHandler = new Handler(dataFetchThread.getLooper(), this);

        dataProcThread = new HandlerThread("DataProcThread");
        dataProcThread.start();
        dataProcHandler = new Handler(dataProcThread.getLooper(), this);

        orientationService.start();
        locationService.start();
        orientationService.registerListener(this);
        locationService.registerListener(this);

        if(!this.isAlive()){
            this.start();
        }
    }

    public void stopApplication() {
        synchronized (pauseLock) {
            pause = true;
        }
        orientationService.unregisterListener(this);
        locationService.unregisterListener(this);
        orientationService.stop();
        locationService.stop();

        dataFetchHandler.removeCallbacksAndMessages(null);
        dataProcHandler.removeCallbacksAndMessages(null);
        dataFetchThread.quitSafely();
        dataProcThread.quitSafely();
        try {
            dataFetchThread.join();
            dataProcThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void quitApplication(){
        stop = true;
    }


    @Override // TODO after onResume, no nodes will be displayed
    public void onLocationChange(Location loc) {
        if (this.loc != null && this.loc.getDistanceCorr(loc) < DIST_THRESHOLD) {
            smallLocationUpdate = true;
        } else {
        }
        this.loc = loc;
        state = State.LOCATION_ACQUIRED;
        Log.i(TAG, "Controller State changed to " + state.name());
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_LOC_VIEW, loc.toString()));
    }

    @Override
    public void onOrientationChange(Orientation values) {
        this.orientation = values;
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_ORIENTATION_VIEW, values.toString()));
    }


    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_UPDATE_OWN_HEIGHT:
                if (loc != null) {
                    int elevations[] = elevationService.getElevation(new Location[]{loc});
                    for (int e : elevations) {
                        Log.d(TAG, "Elevation -> " + e);
                    }
                    loc.setHeight(elevations[0]);
                    dataProcessor.setOwnLocation(loc);
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_LOC_VIEW, loc.toString()));
                    state = State.OWN_HEIGHT_ACQUIRED;
                    Log.i(TAG, "Controller State changed to " + state.name());
                }
                break;

            case MSG_UPDATE_MAPNODES:
                try {
                    mapNodes = mapNodeService.getMapPointsInProximity(loc, tags, MAX_DISTANCE);
                    state = State.NODES_ACQUIRED;
                    Log.i(TAG, "Controller State changed to " + state.name());
                } catch (MissingParameterException e) {
                    e.printStackTrace();
                }
                break;

            case MSG_UPDATE_NODE_HEIGHT:
                if (mapNodes != null) {
                    for (MapNode node : mapNodes) {
                        Log.d(TAG, "acquired node " + node.getName());
                        if (node.getLoc().getHeight() == -1) {
                            node.getLoc().setHeight(elevationService.getElevation(new Location[]{node.getLoc()})[0]); // TODO: change to batch query
                        }
                    }
                    state = State.NODES_HEIGHT_ACQUIRED;
                    Log.i(TAG, "Controller State changed to " + state.name());
                }
                break;

            case MSG_PROCESS_DATA:
                if (mapNodes != null && mapNodes.size() > 0) {
                    List<MarkerDrawable> tempList = new ArrayList<>();
                    for (MapNode node : mapNodes) {
                        Marker marker = dataProcessor.processData(new InputType(node.getLoc(), orientation));
                        marker.setKey(node.getName() + " [" + node.getLoc().getHeight() + "]");
                        tempList.add(new MarkerDrawable(marker));
                    }
                    synchronized (markerList) {
                        markerList = tempList;
                    }
                }
                state = State.DATA_PROCESSING;
                Log.i(TAG, "Controller State changed to " + state.name());
                break;
        }
        return true;
    }

    public List<MarkerDrawable> getMarkerList() {
        return markerList;
    }

    public void setFov(float[] fov) {
        if (dataProcessor != null) {
            dataProcessor.setCameraViewAngleH(fov[0]);
            dataProcessor.setCameraViewAngleV(fov[1]);
        }
    }
}

