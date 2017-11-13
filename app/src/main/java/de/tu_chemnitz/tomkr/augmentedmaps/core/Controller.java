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

import de.tu_chemnitz.tomkr.augmentedmaps.core.types.ApplicationState;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.MissingParameterException;
import de.tu_chemnitz.tomkr.augmentedmaps.core.types.Orientation;
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

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.DIST_THRESHOLD;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MAX_DISTANCE;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_PROCESS_DATA;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_LOC_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_MAPNODES;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_NODE_HEIGHT;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_ORIENTATION_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_OWN_HEIGHT;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_STATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.TARGET_FRAMETIME;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 * <p>
 * Main Application Controller
 */
public class Controller extends Thread implements OrientationListener, LocationListener, Handler.Callback {

    private static final String TAG = Controller.class.getName();
    private ApplicationState state;
    private boolean smallLocationUpdate;

    private final LocationService locationService;
    private final OrientationService orientationService;
    private final DataProcessor dataProcessor;
    private final ElevationService elevationService;
    private final MapNodeService mapNodeService;

    private boolean fetching;

    private Orientation orientation;
    private Location loc;

    private Map<String, List<String>> tags;
    private List<MapNode> mapNodes;
    private List<Marker> markerList;

    private HandlerThread dataFetchThread;
    private Handler dataFetchHandler;
    private HandlerThread dataProcThread;
    private Handler dataProcHandler;
    private Handler mainHandler;

    private boolean stop;
    private boolean pause;
    private final Object pauseLock;
    public static final Object listLock = new Object();

    public Controller(Handler.Callback activityCallback, Context context) {
        tags = Helpers.getTagsFromConfig(context);
        mainHandler = new Handler(Looper.getMainLooper(), activityCallback);
        mapNodeService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
        dataProcessor = DataProcessorProvider.getDataProcessor(DataProcessorProvider.DataProcessorType.A);
        elevationService = ElevationServiceProvider.getElevationService(ElevationServiceProvider.ElevationServiceType.OPEN_ELEVATION);
        locationService = new LocationService(context);
        orientationService = new OrientationService(context);

        state = ApplicationState.INITIALIZED;
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

            if (!fetching) {
                switch (state) {
                    case INITIALIZED:
                        // location will be acquired automatically or call pushLocation now
                        fetching = true;
                        Log.i(TAG, "fetching Location");
                        locationService.pushLocation();
                        break;
                    case LOCATION_ACQUIRED:
                        fetching = true;
                        Log.i(TAG, "fetching own height");
                        dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_OWN_HEIGHT));
                        break;
                    case OWN_HEIGHT_ACQUIRED:
                        if (smallLocationUpdate) {
                            smallLocationUpdate = false;
                            state = ApplicationState.DATA_PROCESSING;
                        } else {
                            fetching = true;
                            Log.i(TAG, "fetching MapNodes");
                            dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_MAPNODES));
                        }
                        break;
                    case NODES_ACQUIRED:
                        fetching = true;
                        Log.i(TAG, "fetching Node Heights");
                        dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_NODE_HEIGHT));
                        break;
                    case NODES_HEIGHT_ACQUIRED:
                    case DATA_PROCESSING:
                        dataProcHandler.sendMessage(dataProcHandler.obtainMessage(MSG_PROCESS_DATA));
                        break;
                }
            }
            mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_VIEW));

            long frametime = (System.currentTimeMillis() - starttime);
            if (frametime < TARGET_FRAMETIME) {
                try {
                    sleep(TARGET_FRAMETIME - frametime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void startApplication() {
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

        stop = false;
        fetching = false;
        synchronized (pauseLock) {
            pause = false;
            pauseLock.notifyAll();
        }

        if (!this.isAlive()) {
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

    public void quitApplication() {
        stop = true;
    }


    @Override // TODO after onResume, no nodes will be displayed
    public void onLocationChange(Location loc) {
        if (this.loc != null && this.loc.getDistanceCorr(loc) < DIST_THRESHOLD) {
            smallLocationUpdate = true;
        }
        this.loc = loc;
        state = ApplicationState.LOCATION_ACQUIRED;
        fetching = false;
        Log.i(TAG, "Controller ApplicationState changed to " + state.name());
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_LOC_VIEW, loc.toString()));
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
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
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_LOC_VIEW, loc.toString()));
                    state = ApplicationState.OWN_HEIGHT_ACQUIRED;
                    fetching = false;
                    Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at MSG_UPDATE_OWN_HEIGHT");
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
                }
                break;

            case MSG_UPDATE_MAPNODES:
                try {
                    mapNodes = mapNodeService.getMapPointsInProximity(loc, tags, MAX_DISTANCE);
                    state = ApplicationState.NODES_ACQUIRED;
                    fetching = false;
                    Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at MSG_UPDATE_MAPNODES");
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
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
                    state = ApplicationState.NODES_HEIGHT_ACQUIRED;
                    fetching = false;
                    Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at MSG_UPDATE_NODE_HEIGHT");
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
                }
                break;

            case MSG_PROCESS_DATA:
                if (mapNodes != null && mapNodes.size() > 0) {
                    List<Marker> tempList = new ArrayList<>();
                    for (MapNode node : mapNodes) {
                        tempList.add(dataProcessor.processData(node, orientation, loc));
                    }
                    synchronized (listLock) {
                        markerList = tempList;
                    }
                }
                if (state != ApplicationState.DATA_PROCESSING) {
                    state = ApplicationState.DATA_PROCESSING;
                    Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at MSG_PROCESS_DATA");
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
                }
                break;
        }
        return true;
    }

    public List<Marker> getMarkerList() {
        return markerList;
    }

    public void setFov(float[] fov) {
        if (dataProcessor != null) {
            dataProcessor.setCameraViewAngleH(fov[0]);
            dataProcessor.setCameraViewAngleV(fov[1]);
        }
    }
}

