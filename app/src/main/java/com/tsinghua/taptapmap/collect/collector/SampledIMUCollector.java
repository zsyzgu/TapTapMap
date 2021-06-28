package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

import com.tsinghua.taptapmap.collect.data.IMUData;
import com.tsinghua.taptapmap.collect.listener.PeriodicSensorEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class SampledIMUCollector extends SensorCollector {
    // For sampled data, keep 5min, 5 * 60 * 10 * 4 = 12k data
    private final int size = 12000;

    private final int samplingPeriod;
    private final int collectPeriod;

    private IMUData data;

    public SampledIMUCollector(Context context, String triggerFolder, int samplingPeriod, int collectPeriod) {
        super(context, triggerFolder);
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

        sensorThread = new HandlerThread("Sampled Thread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());

        sensorManager.registerListener(gyroListener, gyroSensor, samplingPeriod, sensorHandler);
        sensorManager.registerListener(linearAccListener, linearAccSensor, samplingPeriod, sensorHandler);
        sensorManager.registerListener(accListener, accSensor, samplingPeriod, sensorHandler);
        sensorManager.registerListener(magListener, magSensor, samplingPeriod, sensorHandler);
    }

    @Override
    public synchronized void collect() {
        saver.save(data);
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
        return "SampledIMU";
    }
}
    /*
    private SensorEventListener accListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            System.arraycopy(event.values, 0, accMark, 0, accMark.length);
            long timestamp = event.timestamp;
            SensorManager.getRotationMatrix(rotationMatrix, null, accMark, magMark);
            SensorManager.getOrientation(rotationMatrix, orientationAngles);
            // orientationAngles 0 ~ 2: 方位角，俯仰角，倾侧角
            // add complete
            completeSensorInfo.addSensorData(orientationAngles[0], orientationAngles[1], orientationAngles[2], 2, (long)(timestamp / 1e6));
            // add sampled
            count[2]++;
            if (count[2] >= 10) {
                sampledSensorInfo.addSensorData(orientationAngles[0], orientationAngles[1], orientationAngles[2], 2, (long) (timestamp / 1e6));
                count[2] = 0;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener magListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            System.arraycopy(event.values, 0, magMark, 0, magMark.length);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
     */
