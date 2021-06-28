package com.tsinghua.taptapmap.collect.trigger;

import android.content.Context;

import com.tsinghua.taptapmap.collect.collector.BluetoothCollector;
import com.tsinghua.taptapmap.collect.collector.Collector;
import com.tsinghua.taptapmap.collect.collector.CompleteIMUCollector;
import com.tsinghua.taptapmap.collect.collector.LocationClient;
import com.tsinghua.taptapmap.collect.collector.LocationCollector;
import com.tsinghua.taptapmap.collect.collector.NonIMUCollector;
import com.tsinghua.taptapmap.collect.collector.SampledIMUCollector;
import com.tsinghua.taptapmap.collect.collector.WeatherCollector;
import com.tsinghua.taptapmap.collect.collector.WifiCollector;

import java.util.ArrayList;
import java.util.List;

public abstract class Trigger {

    private Context mContext;

    private final int samplingPeriod = 10000;

    public enum CollectorType {
        Bluetooth,
        CompleteIMU,
        SampledIMU,
        NonIMU,
        Location,
        Weather,
        Wifi,
        All
    }

    protected List<Collector> collectors = new ArrayList<>();

    private void initializeAll() {
        String triggerFolder = getName();
        collectors.add(new BluetoothCollector(mContext, triggerFolder));
        collectors.add(new CompleteIMUCollector(mContext, triggerFolder, samplingPeriod, 1));
        collectors.add(new SampledIMUCollector(mContext, triggerFolder, samplingPeriod, 10));
        collectors.add(new NonIMUCollector(mContext, triggerFolder));
        collectors.add(new LocationCollector(mContext, triggerFolder));
        collectors.add(new WeatherCollector(mContext, triggerFolder));
        collectors.add(new WifiCollector(mContext, triggerFolder));
    }

    private void initialize(CollectorType type) {
        String triggerFolder = getName();
        switch (type) {
            case Bluetooth:
                collectors.add(new BluetoothCollector(mContext, triggerFolder));
                break;
            case CompleteIMU:
                collectors.add(new CompleteIMUCollector(mContext, triggerFolder, samplingPeriod, 1));
                break;
            case SampledIMU:
                collectors.add(new SampledIMUCollector(mContext, triggerFolder, samplingPeriod / 10, 10));
                break;
            case NonIMU:
                collectors.add(new NonIMUCollector(mContext, triggerFolder));
                break;
            case Location:
                collectors.add(new LocationCollector(mContext, triggerFolder));
                break;
            case Weather:
                collectors.add(new WeatherCollector(mContext, triggerFolder));
                break;
            case Wifi:
                collectors.add(new WifiCollector(mContext, triggerFolder));
                break;
            case All:
                initializeAll();
                break;
        }
    }

    public Trigger(Context context, List<CollectorType> types) {
        this.mContext = context;
        for (CollectorType type: types) {
            initialize(type);
        }
    }

    public Trigger(Context context, CollectorType type) {
        this.mContext = context;
        initialize(type);
    }

    public abstract void trigger();

    public abstract String getName();
}
