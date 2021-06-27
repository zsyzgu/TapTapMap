package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;

public abstract class SensorCollector extends Collector {

    protected HandlerThread sensorThread;
    protected Handler sensorHandler;
    protected SensorManager sensorManager;

    public SensorCollector(Context context) {
        super(context);
    }

    public abstract void addSensorData(float x, float y, float z, int idx, long time);
}
