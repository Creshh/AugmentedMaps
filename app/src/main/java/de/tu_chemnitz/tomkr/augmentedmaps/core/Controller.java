package de.tu_chemnitz.tomkr.augmentedmaps.core;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import de.tu_chemnitz.tomkr.augmentedmaps.view.MarkerDrawable;

/**
 * Created by Tom Kretzschmar on 05.10.2017.
 *
 * Main Application Controller
 */

public class Controller extends Thread implements OrientationListener, LocationListener, Handler.Callback {

    private static final String TAG = Controller.class.getName();

    public static ReadWriteLock rwLock = new ReentrantReadWriteLock();

    private static final int FRAMETIME = 1000/2;
    private long oldTime = -1;
    private static final int MAX_DISTANCE = 5000;
    private static final float DIST_THRESHOLD = 500;
    public static final int MSG_UPDATE_VIEW = 1;
    public static final int MSG_UPDATE_LOC_VIEW = 2;
    public static final int MSG_UPDATE_ORIENTATION_VIEW = 3;
    private static final int MSG_UPDATE_MAPNODES = 4;
    private static final int MSG_PROCESS_DATA = 5;
    private static final int MSG_UPDATE_OWN_HEIGHT = 6;
    private static final int MSG_UPDATE_HEIGHT = 7;
    private final LocationService locationService;
    private final OrientationService orientationService;

    private Location loc;
    private Map<String, List<String>> tags;


    private List<MapNode> mapNodes;
    private List<MarkerDrawable> markerList;

    private MapNodeService mapNodeService;
    private HandlerThread dataFetchThread;
    private Handler dataFetchHandler;
    private HandlerThread dataProcThread;
    private Handler dataProcHandler;
    private boolean stop;
    private boolean pause;
    private Object pauseLock;

    private Orientation orientation;
    private DataProcessor dataProcessor;
    private ElevationService elevationService;


    private Handler mainHandler;
    private boolean dataProcessed;
    private boolean ownHeightSet;
    private boolean mapNodeHeightSet;
    private boolean bigUpdate;
    private boolean smallUpdate;
    private boolean mapNodeChange;

    public Controller(Handler.Callback activityCallback, Context context) {
        setTags();


        mainHandler = new Handler(Looper.getMainLooper(), activityCallback);


        mapNodeService = MapNodeServiceProvider.getMapPointService(MapNodeServiceProvider.MapPointServiceType.OVERPASS);
        dataProcessor = DataProcessorProvider.getDataProcessor(DataProcessorProvider.DataProcessorType.A);
        elevationService = ElevationServiceProvider.getElevationService(ElevationServiceProvider.ElevationServiceType.OPEN_ELEVATION);
        locationService = new LocationService(context);
        orientationService = new OrientationService(context);

    }

    private void setTags() {
        tags = new HashMap<>();
        tags.put("place", new ArrayList<String>());
        tags.get("place").add("town");
        tags.get("place").add("village");
        tags.get("place").add("city");

        tags.put("natural", new ArrayList<String>());
        tags.get("natural").add("peak");
        tags.get("natural").add("rock");
    }

    @Override
    public void run() {
        Log.d(TAG, "run");
        while (!stop) {

            synchronized (pauseLock) {
                while (pause) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (mapNodeChange) {
                mapNodeChange = false;
                mapNodeHeightSet = false;

                dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_HEIGHT));
            }
            if (ownHeightSet && mapNodeHeightSet) {
                dataProcHandler.sendMessage(dataProcHandler.obtainMessage(MSG_PROCESS_DATA));
            }
            if (smallUpdate) {

                smallUpdate = false;
                ownHeightSet = false;

                dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_OWN_HEIGHT));
            }
            if (bigUpdate) {
                bigUpdate = false;
                ownHeightSet = false;
                dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_MAPNODES));
                dataFetchHandler.sendMessage(dataFetchHandler.obtainMessage(MSG_UPDATE_OWN_HEIGHT));
            }
            if (dataProcessed) { // or markerList not null
                mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_VIEW));
//                dataProcessed = false;
            }




            if(oldTime != -1){
                long current = System.currentTimeMillis();
                try {
                    long sleeptime = FRAMETIME-(current - oldTime);
                    sleep(sleeptime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                oldTime = current;
            }
        }

    }

    public void startApplication() {
        stop = false;
        if(pauseLock == null){
            pauseLock = new Object();
        } else {
            synchronized (pauseLock){
                pause = false;
                pauseLock.notifyAll();
            }
        }

        dataProcessed = false;
        ownHeightSet = false;
        mapNodeHeightSet = false;
        bigUpdate = false;
        smallUpdate = false;
        mapNodeChange = false;

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
        locationService.pushLocation();

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


    @Override
    public void onLocationChange(Location loc) {
        if (this.loc != null && this.loc.getDistanceCorr(loc) < DIST_THRESHOLD) {
            smallUpdate = true;
        } else {
            bigUpdate = true;
        }
        this.loc = loc;
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_LOC_VIEW, loc.toString()));
    }

    @Override
    public void onOrientationChange(Orientation values) {
        this.orientation = values;
        mainHandler.sendMessage(mainHandler.obtainMessage(MSG_UPDATE_ORIENTATION_VIEW, values.toString()));
    }


    @Override
    public boolean handleMessage(Message message) {
//        Log.d(TAG, "handleMessage[" + message.what + "]");
        switch (message.what) {
            case MSG_UPDATE_MAPNODES:
                Log.d(TAG, "Message -> MSG_UPDATE_MAPNODES inbound");
                try {
                    mapNodes = mapNodeService.getMapPointsInProximity(loc, tags, MAX_DISTANCE);
                    mapNodeChange = true;
                } catch (MissingParameterException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Message -> MSG_UPDATE_MAPNODES finished");
                break;
            case MSG_PROCESS_DATA:
//                Log.d(TAG, "Message -> MSG_PROCESS_DATA inbound");
                if (mapNodes != null && mapNodes.size() > 0) {
                    List<MarkerDrawable> tempList = new ArrayList<>();
                    for (MapNode node : mapNodes) {
                        Marker marker = dataProcessor.processData(new InputType(node.getLoc(), orientation));
                        marker.setKey(node.getName() + " [" + node.getLoc().getHeight() + "]");
                        tempList.add(new MarkerDrawable(marker));
                    }
                    if(rwLock.writeLock().tryLock()) {
                        try {
                            markerList = tempList;
                        } finally {
                            rwLock.writeLock().unlock();
                        }
                    }
                }
                dataProcessed = true;
//                Log.d(TAG, "Message -> MSG_PROCESS_DATA finished");
                break;
            case MSG_UPDATE_HEIGHT:
                Log.d(TAG, "Message -> MSG_UPDATE_HEIGHT inbound");
                if (mapNodes != null) {
                    for (MapNode node : mapNodes) {
                        Log.d(TAG, "acquired node " + node.getName());
                        if (node.getLoc().getHeight() == -1) {
                            node.getLoc().setHeight(elevationService.getElevation(new Location[]{node.getLoc()})[0]); // TODO: change to batch query
                        }
                    }
                    mapNodeHeightSet = true;
                }
                Log.d(TAG, "Message -> MSG_UPDATE_HEIGHT finished");
                break;
            case MSG_UPDATE_OWN_HEIGHT:
                Log.d(TAG, "Message -> MSG_UPDATE_OWN_HEIGHT inbound");
                if (loc != null) {
                    int elevations[] = elevationService.getElevation(new Location[]{loc});
                    for (int e : elevations) {
                        Log.d(TAG, "Elevation -> " + e);
                    }
                    loc.setHeight(elevations[0]);
                    dataProcessor.setOwnLocation(loc);
                    ownHeightSet = true;
                }
                Log.d(TAG, "Message -> MSG_UPDATE_OWN_HEIGHT finished");
                break;
            default:
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

