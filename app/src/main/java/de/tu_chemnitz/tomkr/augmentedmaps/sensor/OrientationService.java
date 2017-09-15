package de.tu_chemnitz.tomkr.augmentedmaps.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import de.tu_chemnitz.tomkr.augmentedmaps.datatypes.basetypes.Orientation;

/**
 * Created by Tom Kretzschmar on 01.09.2017.
 *
 */

public class OrientationService implements SensorEventListener{

    private List<OrientationListener> listeners;
    private SensorManager sensorManager ;

    private float[] accelerometerReading = new float[3];
    private float[] magnetometerReading = new float[3];

    private float[] rotationMatrix = new float[9];
    private float[] orientationAngles = new float[3];

    private Sensor acc;
    private Sensor mag;

    Orientation o;

    public OrientationService(Context context){
        listeners = new ArrayList<>();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        start();
    }

    public Orientation getOrientation(){
        Orientation o = new Orientation();

        // Rotation matrix based on current readings from accelerometer and magnetometer.
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        // Express the updated rotation matrix as three orientation angles.
        SensorManager.getOrientation(rotationMatrix , orientationAngles );
        o.setX(orientationAngles [0]); // angle to horizon
        o.setY(orientationAngles [1]);
        o.setZ(orientationAngles [2]); // angle to north
        return o;
    }

    public void registerListener(OrientationListener listener){
        this.listeners.add(listener);
    }

    public void unregisterListener(OrientationListener listener){
        listeners.remove(listener);
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            accelerometerReading = sensorEvent.values;
        if(sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            magnetometerReading = sensorEvent.values;
        o = getOrientation();
        for(OrientationListener listener : listeners){
            listener.onOrientationChange(o);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void stop(){
        sensorManager.unregisterListener(this);
    }

    public void start(){
        sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
