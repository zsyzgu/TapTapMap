package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.provider.Settings;

import com.tsinghua.taptapmap.collect.data.NonIMUData;
import com.tsinghua.taptapmap.collect.listener.NonIMUSensorEventListener;
import com.tsinghua.taptapmap.collect.listener.PeriodicSensorEventListener;

public class NonIMUCollector extends SensorCollector {

    private NonIMUData data;

    public NonIMUCollector(Context context, String triggerFolder) {
        super(context, triggerFolder);
        data = new NonIMUData();
    }

    @Override
    public synchronized void addSensorData(float x, float y, float z, int idx, long time) {
        if (data != null) {
            switch (idx) {
                case Sensor.TYPE_PRESSURE:
                    data.setAirPressure(x);
                    data.setAirPressureTimestamp(time);
                    break;
                case Sensor.TYPE_LIGHT:
                    data.setEnvironmentBrightness(x);
                    data.setEnvironmentBrightnessTimestamp(time);
                    break;
                default:
                    break;
            }
        }
    }

    private NonIMUSensorEventListener pressureListener;
    private NonIMUSensorEventListener lightListener;

    @Override
    public void initialize() {
        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        Sensor pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Sensor light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        pressureListener = new NonIMUSensorEventListener(this, Sensor.TYPE_PRESSURE);
        lightListener = new NonIMUSensorEventListener(this, Sensor.TYPE_LIGHT);

        sensorThread = new HandlerThread("NonIMU Thread", Process.THREAD_PRIORITY_MORE_FAVORABLE);
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
        sensorManager.registerListener(pressureListener, pressure, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
        sensorManager.registerListener(lightListener, light, SensorManager.SENSOR_DELAY_NORMAL, sensorHandler);
    }

    @Override
    public synchronized void collect() {
        data.setScreenBrightness(Settings.System.getInt(mContext.getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,125));
        data.setScreenBrightnessTimestamp(System.currentTimeMillis());
        saver.save(data);
    }

    @Override
    public void close() {
    }

    @Override
    protected String getSaveFolderName() {
        return "NonIMU";
    }
}
