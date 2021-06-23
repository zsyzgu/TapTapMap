package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.amap.api.location.AMapLocation;
import com.tsinghua.taptapmap.collect.data.LocationData;

public class LocationCollector extends Collector {
    private LocationClient client;

    private Context mContext;

    private LocationData data;

    public LocationCollector(Context context) {
        super(context);
        this.mContext = context;
    }

    @Override
    public void initialize() {
        client = LocationClient.getInstance(mContext);
    }

    @Override
    public void collect() {
        //TODO: context.location.trafficInformation
        //TODO: context.location.direction
        AMapLocation mLocation = client.mLocation.clone();
        if (mLocation != null) {
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
