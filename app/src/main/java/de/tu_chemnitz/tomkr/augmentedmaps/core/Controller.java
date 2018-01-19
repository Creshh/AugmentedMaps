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
 * Created by Tom Kretzschmar on 05.11.2017.<br>
 * <br>
 * The main application controller class.<br>
 * This class holds the current {@link ApplicationState} and orchestrates all functions and services of the whole application.<br>
 * It subclasses {@link LooperThread} and holds therefore the main application loop which is called continously. Further it maintains and controls the state of background threads using messages defined in {@link Const}.<br>
 * Also it reacts to system callbacks from the origin activity.<br>
 */
public class Controller extends LooperThread implements OrientationListener, LocationListener, Handler.Callback {
    /**
     * Tag for logging
     */
    private static final String TAG = Controller.class.getName();

    /**
     * The current state of the application. Used mainly for continue processing after system callbacks.
     */
    private ApplicationState state;

    /**
     * Flag used to differ between large and small location updates. On big location updates state will be reset to {@link ApplicationState#LOCATION_ACQUIRED}. See {@link Const#DIST_THRESHOLD} for decision threshold.
     */
    private boolean smallLocationUpdate;

    /**
     * Service used for acquiring the location of the user device.
     */
    private final LocationService locationService;

    /**
     * Service used for acquiring the orientation of the user device.
     */
    private final OrientationService orientationService;

    /**
     * Processor used for calculating all {@link Marker} positions for the augmented reality level.
     */
    private final DataProcessor dataProcessor;

    /**
     * Service used for acquiring the altitude of the user device and of all mapnodes.
     */
    private final ElevationService elevationService;

    /**
     * Service for acquiring mapnodes around the current user device position.
     */
    private final MapNodeService mapNodeService;

    /**
     *
     */
    private boolean fetching;

    /**
     * The current user device location.
     */
    private Location loc;

    /**
     * The current user device orienatation.
     */
    private Orientation orientation;

    /**
     * Map of OSM tags for {@link MapNodeService}
     */
    private Map<String, List<String>> tags;

    /**
     * List of all MapNodes which will be processed.
     */
    private List<MapNode> mapNodes;

    /**
     * List of all Markers which are the result of the ongoing processing of MapNodes.
     */
    private List<Marker> markerList;

    /**
     * Thread for fetching data, including {@link MapNodeService} and {@link ElevationService}.
     */
    private HandlerThread dataFetchThread;

    /**
     * Handler, which sends messages to the {@link #dataFetchThread}.
     */
    private Handler dataFetchHandler;

    /**
     * Thread for processing data, including {@link DataProcessor}.
     */
    private HandlerThread dataProcThread;

    /**
     * Handler, which sends messages to the {@link #dataProcThread}.
     */
    private Handler dataProcHandler;

    /**
     * Handler, which sends messages to the main thread. Will call the activityCallback set in {@link #Controller(Handler.Callback, Context, Camera2)}.
     */
    private Handler mainHandler;

    /**
     * Camera2 implementation for setting {@link android.media.ImageReader.OnImageAvailableListener}
     */
    private final Camera2 camera;

    /**
     * Synchronization object for accessing {@link #markerList}
     */
    public static final Object listLock = new Object();

    /**
     * Flag, if set data is logged to file.
     */
    private boolean logData = false;

    /**
     * Timestamp indicating the start of the logging procedure.
     */
    private long logStart;

    /**
     * Initial Log value containing only the relevant x and y values to normalize the logged output.
     */
    private Vec2f initialLog;

    /**
     * List which contains the contiously logged values. Will be written to file after logging is finished.
     */
    private ArrayList<TimedVec2f> dataLog;

    /**
     * Full constructor. There must be only one Controller in the whole Application.<br>
     * Acquires all services needed and sets the handler to the main thread.
     * @param activityCallback Callback which handles messages to the main thread respectively the view classes.
     * @param context The context of the calling activity.
     * @param camera A Camera2 implementation for setting the {@link android.media.ImageReader.OnImageAvailableListener}
     */
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

    /**
     * Main application loop which involves all of the orchestration. No processing is done in this Thread.
     */
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
                case OWN_ALTITUDE_ACQUIRED:
                    if (smallLocationUpdate) {
                        smallLocationUpdate = false;
                        state = ApplicationState.DATA_PROCESSING;
                        Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at OWN_ALTITUDE_ACQUIRED");
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
                case NODES_ALTITUDE_ACQUIRED:
                case DATA_PROCESSING:
                    dataProcHandler.sendMessage(dataProcHandler.obtainMessage(MSG_PROCESS_DATA));
                    break;
            }
        }
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_VIEW));
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_FPS_VIEW, super.getFrametime() + " | " + super.getFPS()));
    }

    /**
     * Start application main loop and all background threads. Further start all initially used services and register listeners for them.
     */
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

    /**
     * Pause application main loop, unregister listeners, pause services. Further remove callbacks and stop background threads.
     */
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

    /**
     * Quit Application. Can't be restarted after that. Before calling this make sure to call {@link #stopApplication()}.
     */
    public void quitApplication() {
        super.quit();
    }


    /**
     * Callback method for {@link #dataFetchThread} and {@link #dataProcThread}. Handles all messages sent in main application loop and does the corresponding processing.
     * @param message Message object which should be handled.
     * @return true if message could be handled, false otherwise
     */
    @Override
    public boolean handleMessage(Message message) {
        switch (message.what) {
            case MSG_UPDATE_OWN_HEIGHT:
                if (loc != null) {
                    loc = elevationService.getElevation(loc);
                    Log.d(TAG, "Elevation -> " + loc.getAlt());
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_LOC_VIEW, loc.toString()));
                    state = ApplicationState.OWN_ALTITUDE_ACQUIRED;
                    fetching = false;
                    Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at MSG_UPDATE_OWN_HEIGHT");
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
                }
                return true;

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
                return true;

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
                    state = ApplicationState.NODES_ALTITUDE_ACQUIRED;
                    fetching = false;
                    Log.i(TAG, "Controller ApplicationState changed to " + state.name() + " at MSG_UPDATE_NODE_HEIGHT");
                    mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_STATE_VIEW, state.name()));
                }
                return true;

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
                return true;
            default:
                return false;
        }
    }

    /**
     * @return List of all processed markers.
     */
    public List<Marker> getMarkerList() {
        return markerList;
    }

    /**
     * Callback for {@link LocationService}. Sets current location and notifies mainHandler to update debug views.
     */
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


    /**
     * Callback for {@link OrientationService}. Sets current orientation and notifies mainHandler to update debug view.
     */
    @Override
    public void onOrientationChange(Orientation orientation) {
        this.orientation = orientation;
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_ORIENTATION_VIEW, orientation.toString()));
    }

    /**
     * Set the currently activated setting in the {@link OrientationService}.
     * @param flag OrientationFlag which should be set.
     */
    public void setOrientationFlag(OrientationService.Flag flag) {
        if (this.orientationService != null) {
            this.orientationService.setFlag(flag);
        }
    }

    /**
     * Start logging orientation values. After a defined time {@link Const#LOG_TIME} the results are written to file.
     */
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

