package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.tsinghua.taptapmap.collect.data.LocationData;

public class LocationCollector extends Collector {
    private LocationClient client;

    private LocationData data;

    public LocationCollector(Context context, String triggerFolder) {
        super(context, triggerFolder);
    }

    @Override
    public void initialize() {
        client = LocationClient.getInstance(mContext);
    }

    @Override
    public void collect() {
        if (client.mLocation != null) {
            AMapLocation mLocation = client.mLocation.clone();
            data = new LocationData(
                    mLocation.getLongitude(),
                    mLocation.getLatitude(),
                    mLocation.getAltitude(),
                    mLocation.getAccuracy(),
                    mLocation.getFloor(),
                    mLocation.getCity(),
                    mLocation.getPoiName(),
                    mLocation.getStreet()
            );
            saver.save(data);
        } else {
            Log.e("DATA", "NULL");
        }
    }

    @Override
    public void close() {

    }

    @Override
    protected String getSaveFolderName() {
        return "Location";
    }
}
