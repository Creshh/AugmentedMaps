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
import de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing.MotionAnalyzer;
import de.tu_chemnitz.tomkr.augmentedmaps.imageprocessing.MotionAnalyzerProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessor;
import de.tu_chemnitz.tomkr.augmentedmaps.processing.DataProcessorProvider;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.LocationService;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationListener;
import de.tu_chemnitz.tomkr.augmentedmaps.sensor.OrientationService;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Helpers;
import de.tu_chemnitz.tomkr.augmentedmaps.util.Vec2f;

import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.DIST_THRESHOLD;
import static de.tu_chemnitz.tomkr.augmentedmaps.core.Constants.LOW_PASS_FACTOR;
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
    private final Camera2 camera;
    private MotionAnalyzer motionAnalyzer;
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
    private OpenCVHandler openCVHandler;
    public boolean lowPass;
    public boolean logData = false;
    public long logStart;
    private ArrayList<Vec2f> dataLog;
    public boolean motion;
    private float fovH = 10;
    private Vec2f motionVec;

    public Controller(Handler.Callback activityCallback, Context context, Camera2 camera) {
        tags = Helpers.getTagsFromConfig(context);
        mainHandler = new Handler(Looper.getMainLooper(), activityCallback);
        mapNodeService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
        dataProcessor = DataProcessorProvider.getDataProcessor(DataProcessorProvider.DataProcessorType.A);
        elevationService = ElevationServiceProvider.getElevationService(ElevationServiceProvider.ElevationServiceType.OPEN_ELEVATION);
        locationService = new LocationService(context);
        orientationService = new OrientationService(context);
        motionAnalyzer = MotionAnalyzerProvider.getMotionAnalyzer(MotionAnalyzerProvider.MotionAnalyzerType.A);
        openCVHandler = new OpenCVHandler();

        this.camera = camera;

        state = ApplicationState.INITIALIZED;
        pauseLock = new Object();
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
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

                    motionVec = motionAnalyzer.getRelativeMotionVector(camera.getCurrentImage(), openCVHandler);
                    Orientation temp = new Orientation();
                    if (motionVec != null && motionVec.getX() != 0 && motionVec.getY() != 0) {
                        temp.setX(motion(-1, this.orientation.getX(), motionVec.getX(), fovH));
//                    temp.setY(motion(values.getY(), this.orientation.getY(), motionVec.getY()));
                        temp.setY(orientation.getY());
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

        this.fovH = fov[0];
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
        Orientation temp = new Orientation();
        if(lowPass){
            temp.setX(lowPass(values.getX(), this.orientation.getX()));
            temp.setY(lowPass(values.getY(), this.orientation.getY()));
            temp.setZ(lowPass(values.getZ(), this.orientation.getZ()));
            this.orientation = temp;
//        } else if(motion) {
//
////            Vec2f motionVec = null;
////            try {
////                motionVec = motionAnalyzer.getRelativeMotionVector(camera.getCurrentImage(), openCVHandler);
////            } catch (Exception e){
////                e.printStackTrace();
////            }
//            if(motionVec != null && motionVec.getX() != 0 && motionVec.getY() != 0) {
//                temp.setX(motion(values.getX(), this.orientation.getX(), motionVec.getX(), fovH));
////                temp.setY(motion(values.getY(), this.orientation.getY(), motionVec.getY()));
//                temp.setY(values.getY());
//            } else {
//                temp.setX(values.getX());
//                temp.setY(values.getY());
//            }
        } else if (!motion){
            temp = values;
            this.orientation = temp;
        }
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_ORIENTATION_VIEW, values.toString() + System.lineSeparator() + temp.toString()));
    }

    /**
     * IMPORTANT: Don't call multiple times for the same motion value, or result will be wrong. Has to be called each time a new motionValue is obtained from MotionAnalyzer
     *
     * @param newValue
     * @param oldValue
     * @param motionValue
     * @param fov
     * @return
     */
    private float motion(float newValue, float oldValue, float motionValue, float fov){
        // TODO: calculate angular motion from vector in display coordinates.
        // TODO: move to separate module
        // TODO: or calculate estimated Marker movement, and take it to consideration when updating all markers -> then the marker position must be evaluated, and not the orientation values!!!


//        float diff = newValue-oldValue;
        float angle = motionValue * fov; // motionValue is in [0..1]
        Log.d(TAG, "sensorAngle: " + /*diff*/ 0 + " motionAngle: " + angle + " from value: " + motionValue + " with fov: " + fov);


        float intervalTest = 0; // TODO: calculate the motion in 0..1 like in dataprocessor to test correctness

        return oldValue - angle;
    }

    /**
     * Low-pass filter, which smoothes the newValue values. TODO: Move to separate Module
     */
    private float lowPass(float newValue, float oldValue) {
        float output = newValue;
        if (oldValue != 0) {
            float diff = newValue - oldValue;
            if(Math.abs(diff) < 180) {
                output = oldValue + (LOW_PASS_FACTOR * diff);
//            Log.d(TAG, "old:" + oldValue + " new:" + newValue + " out:" + output);
            }
        }
        return output;
    }

    public void log() { // TODO: separate button for debugging -> start from actual orientation and then log orientation and with motionanalyzer estimated orientation separaetely -> move -> check how much these values differ
        // TODO: maybe Log defined Point, not Orientation values. Evaluate correct calculated position. maybe use image analysation for new position estimation, and measure the correctness using distance between closest points (maybe with only 1 point)
        logStart = System.currentTimeMillis();
        if(dataLog == null){
            dataLog = new ArrayList<>();
        } else {
            dataLog.clear();
        }
        logData = true;
    }
}

