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
import de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing.ImageProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing.ImageProcessorProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessorProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationService;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.SensorFilter;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.DIST_THRESHOLD;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.LOW_PASS_FACTOR;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MAX_DISTANCE;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_PROCESS_DATA;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_FPS_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_LOC_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_MAPNODES;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_NODE_HEIGHT;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_ORIENTATION_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_OWN_HEIGHT;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_STATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.MSG_UPDATE_VIEW;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.TARGET_FRAMETIME;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.TICKS_PER_SECOND;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 * <p>
 * Main Application Controller
 */
public class Controller extends Thread implements OrientationListener, LocationListener, Handler.Callback {

    private static final String TAG = Controller.class.getName();
    private ImageProcessor imageProcessor;
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
    public boolean lowPass;
    private boolean logData = false;
    private long logStart;
    private ArrayList<Vec2f> dataLog;
    public boolean motion;
    private float fov[];

    public Controller(Handler.Callback activityCallback, Context context, Camera2 camera) {
        tags = Helpers.getTagsFromConfig(context);
        mainHandler = new Handler(Looper.getMainLooper(), activityCallback);
        mapNodeService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
        dataProcessor = DataProcessorProvider.getDataProcessor(DataProcessorProvider.DataProcessorType.A);
        elevationService = ElevationServiceProvider.getElevationService(ElevationServiceProvider.ElevationServiceType.OPEN_ELEVATION);
        locationService = new LocationService(context);
        orientationService = new OrientationService(context);
        imageProcessor = ImageProcessorProvider.getMotionAnalyzer(ImageProcessorProvider.MotionAnalyzerType.OPTICAL_FLOW);

        camera.registerImageAvailableListener(imageProcessor);

        state = ApplicationState.INITIALIZED;
        pauseLock = new Object();
    }

    @Override
    public void run() {
        while (!stop) {
            long starttime = System.currentTimeMillis();
            if(logData) {
                if (logStart + Constants.LOG_TIME < starttime) {
                    logData = false;
                    Helpers.saveLogToFile(dataLog);
                } else {
                    dataLog.add(new Vec2f(this.orientation.getX(), this.orientation.getY()));
                }
            }
            synchronized (pauseLock) {
                while (pause) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if(motion) {
                try {

                    Vec2f motionVec = imageProcessor.getRelativeMotionVector();
                    Orientation temp = new Orientation();
                    if (orientation != null && motionVec != null && motionVec.getX() != 0 && motionVec.getY() != 0) {
                        temp.setX(SensorFilter.fusion(-1, this.orientation.getX(), motionVec.getX(), fov[0]));
                        temp.setY(SensorFilter.fusion(-1, this.orientation.getY(), motionVec.getY(), fov[1]));
                        orientation = temp;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
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

            long frametime = (System.currentTimeMillis() - starttime);
            int fps = frametime != 0 ? (int) (1000/frametime) : TARGET_FRAMETIME;
            if (frametime < TARGET_FRAMETIME) {
                try {
                    sleep(TARGET_FRAMETIME - frametime);
                    fps = TICKS_PER_SECOND;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_FPS_VIEW, frametime + " | " + fps));
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
                    for (int i = 0; i< mapNodes.size(); i++) {
                        locs[i] = mapNodes.get(i).getLoc();
                    }
                    locs = elevationService.getElevation(locs);
                    for (int i = 0; i< mapNodes.size(); i++) {
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

        this.fov = fov;
    }


    @Override // TODO: Fix: after onResume, no nodes will be displayed
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
        Orientation temp = new Orientation();
        if(lowPass){
            temp.setX(SensorFilter.lowPass(values.getX(), this.orientation.getX()));
            temp.setY(SensorFilter.lowPass(values.getY(), this.orientation.getY()));
            temp.setZ(SensorFilter.lowPass(values.getZ(), this.orientation.getZ()));
            this.orientation = temp;
        } else if (!motion){
            temp = values;
            this.orientation = temp;
        }
        if(values != null && orientation != null)
            mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_ORIENTATION_VIEW, values.toString() + System.lineSeparator() + orientation.toString()));
    }


    // TODO: separate button for debugging -> start from actual orientation and then logToFile orientation and with motionanalyzer estimated orientation separaetely -> move -> check how much these values differ
    // TODO: maybe Log defined Point, not Orientation values. Evaluate correct calculated position. maybe use image analysation for new position estimation, and measure the correctness using distance between closest points (maybe with only 1 point)
    public void logToFile() {
        logStart = System.currentTimeMillis();
        if(dataLog == null){
            dataLog = new ArrayList<>();
        } else {
            dataLog.clear();
        }
        logData = true;
    }
}

