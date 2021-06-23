package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import com.tsinghua.taptapmap.collect.data.SensorData;
import com.tsinghua.taptapmap.collect.sensor.PeriodicSensorEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CompleteSensorCollector extends SensorCollector {
    // For complete data, keep 1min, 1 * 60 * 100 * 4 = 24k data
    private int size = 24000;

    private final int samplingPeriod;
    private final int collectPeriod;

    private SensorData data;

    public CompleteSensorCollector(Context context, int samplingPeriod, int collectPeriod) {
        super(context);
        this.samplingPeriod = samplingPeriod;
        this.collectPeriod = collectPeriod;
        this.data = new SensorData();
    }

    public synchronized void addSensorData(float x, float y, float z, int idx, long time) {
        data.insert(new ArrayList<>(Arrays.asList(
                x, y, z,
                (float) (time % 1000),
                (float) idx
        )), size);
    }

    @Override
    public void initialize() {
        SensorManager sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        SensorEventListener gyroListener = new PeriodicSensorEventListener(this, Sensor.TYPE_GYROSCOPE, collectPeriod);
        SensorEventListener linearAccListener = new PeriodicSensorEventListener(this, Sensor.TYPE_LINEAR_ACCELERATION, collectPeriod);
        SensorEventListener accListener = new PeriodicSensorEventListener(this, Sensor.TYPE_ACCELEROMETER, collectPeriod);
        SensorEventListener magListener = new PeriodicSensorEventListener(this, Sensor.TYPE_MAGNETIC_FIELD, collectPeriod);

        sensorManager.registerListener(gyroListener, gyroSensor, samplingPeriod);
        sensorManager.registerListener(linearAccListener, linearAccSensor, samplingPeriod);
        sensorManager.registerListener(accListener, accSensor, samplingPeriod);
        sensorManager.registerListener(magListener, magSensor, samplingPeriod);
    }

    @Override
    public synchronized void collect() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    saver.save(data);
                }
            }
        }, 30000);
    }

    @Override
    public void close() {

    }

    @Override
    protected String getSaveFolderName() {
        return "CompleteSensor";
    }
}
