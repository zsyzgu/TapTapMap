package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;

public abstract class SensorCollector extends Collector {
    public SensorCollector(Context context) {
        super(context);
    }

    public abstract void addSensorData(float x, float y, float z, int idx, long time);
}
