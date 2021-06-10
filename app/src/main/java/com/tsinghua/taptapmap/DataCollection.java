package com.tsinghua.taptapmap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataCollection extends Activity {
    private Button mButtonDataCollection;
    private TextView mTvDataCollection;
    private AMapLocationClient mLocationClient;
    private AMapLocation mLocation;

    public abstract class Info {
        public String queryTime;
        public String responseTime;

        public Info() {
            queryTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date());
        }

        public abstract void collectData();

        protected void save() {
            responseTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date());
            String filePath = "/storage/emulated/0/";
            String fileName = getClass().getName() + ".json";
            String result = (new Gson()).toJson(this);
            mTvDataCollection.setText(mTvDataCollection.getText() + "\n" + result);

            try {
                File file = new File(filePath);
                if (!file.exists())
                    file.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                File file = new File(filePath + fileName);
                if (!file.exists())
                    file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }

            File file = new File(filePath, fileName);
            BufferedWriter bufferedWriter = null;
            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true))); // true -- append, false -- overwrite
                bufferedWriter.write(result);
                bufferedWriter.newLine();
                Log.d("taptap", "abc");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if(bufferedWriter != null){
                        bufferedWriter.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public class WeatherInfo extends Info {
        String reportTime;
        String weather;
        String windDirection;
        String windStrength;
        float windSpeed;
        float temperature;
        float humidity;
        int airQuality;

        @Override
        public void collectData() {
            if (mLocation != null && !mLocation.getCity().equals("")) {
                WeatherSearchQuery queryLive = new WeatherSearchQuery(mLocation.getCity(), WeatherSearchQuery.WEATHER_TYPE_LIVE);
                WeatherSearch weatherSearchLive = new WeatherSearch(DataCollection.this);
                weatherSearchLive.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
                    @Override
                    public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
                        LocalWeatherLive result = localWeatherLiveResult.getLiveResult();
                        reportTime = result.getReportTime();
                        weather = result.getWeather();
                        windDirection = result.getWindDirection();
                        windStrength = result.getWindPower();
                        temperature = Float.parseFloat(result.getTemperature());
                        humidity = Float.parseFloat(result.getHumidity());
                        //TODO: context.weather.airQuality
                        save(); // 保存数据到文件
                    }

                    @Override
                    public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

                    }
                });
                weatherSearchLive.setQuery(queryLive);
                weatherSearchLive.searchWeatherAsyn();
            }
        }
    }

    public class LocationInfo extends Info {
        double longitude;
        double latitude;
        float altitude;
        float direction;
        float accuracy;
        String floor;
        String city;
        String poiName;
        String street;
        String trafficInformation;

        @Override
        public void collectData() {
            longitude = mLocation.getLongitude();
            latitude = mLocation.getLatitude();
            altitude = (float)mLocation.getAltitude();
            poiName = mLocation.getPoiName();
            city = mLocation.getCity();
            street = mLocation.getStreet();
            floor = mLocation.getFloor();
            accuracy = mLocation.getAccuracy();
            //TODO: context.location.trafficInformation
            //TODO: context.location.direction
            save(); // 保存数据到文件
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);
        mButtonDataCollection = findViewById(R.id.button_data_collection);
        mTvDataCollection = findViewById(R.id.tv_data_collection);
        initService();

        mButtonDataCollection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTvDataCollection.setText("");
                collectData();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
    }

    private void initService() {
        // 开启定位服务
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setInterval(2000);
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation != null) {
                    mLocation = aMapLocation.clone();
                }
            }
        });
    }

    private void stopService() {
        // 停止定位服务
        mLocationClient.stopLocation();
    }

    private void collectData() {
        new WeatherInfo().collectData(); // 收集天气数据，并附加在WeatherInfo.json中
        new LocationInfo().collectData(); // 收集位置数据，并附加在LocationInfo.json中
    }
}