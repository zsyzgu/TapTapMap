package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

public class LocationClient {
    private volatile static LocationClient mInstance;

    private static AMapLocationClient mLocationClient;

    public volatile AMapLocation mLocation;

    private LocationClient(Context mContext) {
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setInterval(2000);
        mLocationClient = new AMapLocationClient(mContext);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
        mLocationClient.setLocationListener(aMapLocation -> {
            if (aMapLocation != null) {
                mLocation = aMapLocation.clone();
            }
        });
    }

    public static LocationClient getInstance(Context mContext) {
        if (mInstance == null) {
            synchronized (LocationClient.class) {
                if (mInstance == null) {
                    mInstance = new LocationClient(mContext);
                }
            }
        }
        return mInstance;
    }

    public static void close() {
        if (mInstance != null) {
            mLocationClient.stopLocation();
        }
    }
}
