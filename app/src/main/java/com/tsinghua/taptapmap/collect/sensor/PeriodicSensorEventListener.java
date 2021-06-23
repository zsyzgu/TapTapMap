package com.tsinghua.taptapmap.collect.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

import com.tsinghua.taptapmap.collect.collector.SensorCollector;

public class PeriodicSensorEventListener implements SensorEventListener {

    private int period;
    private int counter;
    private int sensorType;
    private SensorCollector collector;

    public PeriodicSensorEventListener(SensorCollector collector, int sensorType, int period) {
        this.sensorType = sensorType;
        this.period = period;
        this.collector = collector;

        this.counter = 0;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (counter == 0) {
            this.collector.addSensorData(event.values[0], event.values[1], event.values[2], sensorType, (long)(event.timestamp / 1e6));
            counter = period - 1;
        } else {
            counter -= 1;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
