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

import de.tu_chemnitz.tomkr.augmentedmaps.camera.Camera2;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.ApplicationState;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Location;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MapNode;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Marker;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.MissingParameterException;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Orientation;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.ElevationServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.MapNodeService;
import de.tu_chemnitz.tomkr.augmentedmaps.dataprovider.MapNodeServiceProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessorProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.location.LocationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.location.LocationService;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.TimedVec2f;
import de.tu_chemnitz.tomkr.augmentedmaps.core.datatypes.Vec2f;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.DIST_THRESHOLD;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.LOG_TIME;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MAX_DISTANCE;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_PROCESS_DATA;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_FPS_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_INFO_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_LOC_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_MAPNODES;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_NODE_HEIGHT;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_ORIENTATION_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_OWN_HEIGHT;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_STATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.MSG_UPDATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Const.TARGET_FRAMETIME;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 * <p>
 * Main Application Controller
 */
public class Controller extends LooperThread implements OrientationListener, LocationListener, Handler.Callback {

    private static final String TAG = Controller.class.getName();
    private ApplicationState state;
    private boolean smallLocationUpdate;

    private final LocationService locationService;
    private final OrientationService orientationService;
    private final DataProcessor dataProcessor;
    private final ElevationService elevationService;
    private final MapNodeService mapNodeService;

    private boolean fetching;

    private Location loc;

    private Map<String, List<String>> tags;
    private List<MapNode> mapNodes;
    private List<Marker> markerList;

    private HandlerThread dataFetchThread;
    private Handler dataFetchHandler;
    private HandlerThread dataProcThread;
    private Handler dataProcHandler;
    private Handler mainHandler;
    private final Camera2 camera;

    public static final Object listLock = new Object();
    private boolean logData = false;
    private long logStart;
    private Vec2f initialLog;
    private ArrayList<TimedVec2f> dataLog;
    private Orientation orientation;

    public Controller(Handler.Callback activityCallback, Context context, Camera2 camera) {
        super(TARGET_FRAMETIME);
        tags = Helpers.getTagsFromConfig(context);
        mainHandler = new Handler(Looper.getMainLooper(), activityCallback);
        mapNodeService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
        dataProcessor = DataProcessorProvider.getDataProcessor(DataProcessorProvider.DataProcessorType.A);
        elevationService = ElevationServiceProvider.getElevationService(ElevationServiceProvider.ElevationServiceType.OPEN_ELEVATION);
        locationService = new LocationService(context);
        orientationService = new OrientationService(context);
        orientationService.registerListener(this);
        state = ApplicationState.INITIALIZED;
        this.camera = camera;
    }

    @Override
    protected void loop() {
        if (logData) {
            if (logStart + Const.LOG_TIME < System.currentTimeMillis()) {
                // save log to file when finished logging
                logData = false;
                initialLog = null;
                String logFile = Helpers.saveLogToFile(dataLog, orientationService.getFlag().name());
                mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_INFO_VIEW, "Logfile " + logFile + " written."));
            } else {
                // apply continous data to log
                TimedVec2f current = new TimedVec2f(System.currentTimeMillis() - logStart, this.orientation.getX(), this.orientation.getY());
                if (initialLog == null) {
                    initialLog = current;
                } else {
                    current.substract(initialLog);
                    dataLog.add(current);
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
                        Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at OWN_HEIGHT_ACQUIRED");
                        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
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
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_FPS_VIEW, super.getFrametime() + " | " + super.getFPS()));
    }

    public void startApplication() {
        smallLocationUpdate = false;

        dataFetchThread = new HandlerThread("DataFetchThread");
        dataFetchThread.start();
        dataFetchHandler = new Handler(dataFetchThread.getLooper(), this);

        dataProcThread = new HandlerThread("DataProcThread");
        dataProcThread.start();
        dataProcHandler = new Handler(dataProcThread.getLooper(), this);

        camera.registerImageAvailableListener(orientationService.getImageProcessor());
        orientationService.startLooper();
        locationService.start();
        orientationService.registerListener(this);
        locationService.registerListener(this);

        fetching = false;

        super.startLooper();
    }

    public void stopApplication() {
        super.pause();
        orientationService.unregisterListener(this);
        locationService.unregisterListener(this);
        orientationService.pause();
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
        super.quit();
    }


    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_UPDATE_OWN_HEIGHT:
                if (loc != null) {
                    loc = elevationService.getElevation(loc);
                    Log.d(TAG, "Elevation -> " + loc.getHeight());
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
                    Location[] locs = new Location[mapNodes.size()];
                    for (int i = 0; i < mapNodes.size(); i++) {
                        locs[i] = mapNodes.get(i).getLoc();
                    }
                    locs = elevationService.getElevation(locs);
                    for (int i = 0; i < mapNodes.size(); i++) {
                        Log.d(TAG, "acquired node height " + mapNodes.get(i).getName());
                        mapNodes.get(i).setLoc(locs[i]);
                    }
                    state = ApplicationState.NODES_HEIGHT_ACQUIRED;
                    fetching = false;
                    Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at MSG_UPDATE_NODE_HEIGHT");
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
                }
                break;

            case MSG_PROCESS_DATA:
                if (mapNodes != null && mapNodes.size() > 0) {
                    synchronized (listLock) {
                        markerList = dataProcessor.processData(mapNodes, orientation, loc);
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

    @Override // TODO: Fix: after onResume, no nodes will be displayed???????
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
    public void onOrientationChange(Orientation orientation) {
        this.orientation = orientation;
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_ORIENTATION_VIEW, orientation.toString()));
    }

    public void setFlag(OrientationService.Flag flag) {
        if (this.orientationService != null) {
            this.orientationService.setFlag(flag);
        }
    }

    public void logToFile() {
        Log.d(TAG, "start Logging");
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_INFO_VIEW, "started Logging for " + (LOG_TIME/1000) + "s."));
        logStart = System.currentTimeMillis();
        if (dataLog == null) {
            dataLog = new ArrayList<>();
        } else {
            dataLog.clear();
        }
        logData = true;
    }
}

