package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.util.Log;

import com.tsinghua.taptapmap.collect.data.IMUData;
import com.tsinghua.taptapmap.collect.listener.PeriodicSensorEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class CompleteIMUCollector extends SensorCollector {
    // For complete data, keep 1min, 1 * 60 * 100 * 4 = 24k data
    private int size = 24000;

    private final int samplingPeriod;
    private final int collectPeriod;

    private IMUData data = null;


    public CompleteIMUCollector(Context context, int samplingPeriod, int collectPeriod) {
        super(context);
        this.samplingPeriod = samplingPeriod;
        this.collectPeriod = collectPeriod;
        this.data = new IMUData();
    }

    public synchronized void addSensorData(float x, float y, float z, int idx, long time) {
        if (data != null) {
            data.insert(new ArrayList<>(Arrays.asList(
                    x, y, z,
                    (float) (time % 1000),
                    (float) idx
            )), size);
        }
    }
    
    private PeriodicSensorEventListener gyroListener;
    private PeriodicSensorEventListener linearAccListener;
    private PeriodicSensorEventListener accListener;
    private PeriodicSensorEventListener magListener;

    @Override
    public void initialize() {
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        gyroListener = new PeriodicSensorEventListener(this, Sensor.TYPE_GYROSCOPE, collectPeriod);
        linearAccListener = new PeriodicSensorEventListener(this, Sensor.TYPE_LINEAR_ACCELERATION, collectPeriod);
        accListener = new PeriodicSensorEventListener(this, Sensor.TYPE_ACCELEROMETER, collectPeriod);
        magListener = new PeriodicSensorEventListener(this, Sensor.TYPE_MAGNETIC_FIELD, collectPeriod);

        sensorThread = new HandlerThread("Complete Thread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
        sensorManager.registerListener(gyroListener, gyroSensor, samplingPeriod, sensorHandler);
        sensorManager.registerListener(linearAccListener, linearAccSensor, samplingPeriod, sensorHandler);
        sensorManager.registerListener(accListener, accSensor, samplingPeriod, sensorHandler);
        sensorManager.registerListener(magListener, magSensor, samplingPeriod, sensorHandler);

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
        sensorManager.unregisterListener(gyroListener);
        sensorManager.unregisterListener(linearAccListener);
        sensorManager.unregisterListener(accListener);
        sensorManager.unregisterListener(magListener);
        sensorThread.quitSafely();
    }

    @Override
    protected String getSaveFolderName() {
        return "CompleteIMU";
    }
}
