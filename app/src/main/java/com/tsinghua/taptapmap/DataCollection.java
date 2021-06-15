package com.tsinghua.taptapmap;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
//            mTvDataCollection.setText(mTvDataCollection.getText() + "\n" + result);

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

    public class CompleteSensorInfo extends Info{
        private List<Long> shouldSaveTime = new ArrayList<>();

        // For complete data, keep 1min, 1 * 60 * 100 * 3 = 18k data
        private int size = 18000;
        private float[][] sensorData = new float[size][4];

        public void addSensorData(float x, float y, float z, int idx, long time) {
            for (int i = 0; i < size - 1; i++)
                System.arraycopy(sensorData[i + 1], 0, sensorData[i], 0, 4);
            sensorData[size - 1][0] = x;
            sensorData[size - 1][1] = y;
            sensorData[size - 1][2] = z;
            sensorData[size - 1][3] = (float)(time % 1000) * 100 + idx + 1;
            // save [-30s, 30s] sensor data
            if (shouldSaveTime.size() > 0 && System.currentTimeMillis() - shouldSaveTime.get(0) > 30 * 1e3) {
                shouldSaveTime.remove(0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        save();   // 保存数据到文件
                    }
                }).start();
            }
        }

        public void collectData() {
            shouldSaveTime.add(System.currentTimeMillis());
        }
    }

    public class SampledSensorInfo extends Info {
        // For sampled data, keep 5min, 5 * 60 * 10 * 3 = 9k data
        private int size = 9000;
        private float[][] sensorData = new float[size][4];

        public void addSensorData(float x, float y, float z, int idx, long time) {
            for (int i = 0; i < size - 1; i++)
                System.arraycopy(sensorData[i + 1], 0, sensorData[i], 0, 4);
            sensorData[size - 1][0] = x;
            sensorData[size - 1][1] = y;
            sensorData[size - 1][2] = z;
            sensorData[size - 1][3] = (float)(time % 1000) * 100 + idx + 1;
        }

        @Override
        public void collectData() {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    save();   // 保存数据到文件
                }
            }).start();
        }
    }

    // 传感器数据收集相关
    private CompleteSensorInfo completeSensorInfo;
    private SampledSensorInfo sampledSensorInfo;
    private int[] count = new int[]{0, 0, 0};

    private SensorManager sensorManager;
    private int samplingPeriod = 10000;

    private final float[] accMark = new float[3];
    private final float[] magMark = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

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

        // 开启传感器监听&收集
        completeSensorInfo = new CompleteSensorInfo();
        sampledSensorInfo = new SampledSensorInfo();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        Sensor gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        sensorManager.registerListener(gyroListener, gyroSensor, samplingPeriod);
        sensorManager.registerListener(linearAccListener, linearAccSensor, samplingPeriod);
        sensorManager.registerListener(accListener, accSensor, samplingPeriod);
        sensorManager.registerListener(magListener, magSensor, samplingPeriod);
    }

    private SensorEventListener gyroListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // add complete
            completeSensorInfo.addSensorData(event.values[0], event.values[1], event.values[2], 0, (long)(event.timestamp / 1e6));
            // add sampled
            count[0]++;
            if (count[0] >= 10) {
                sampledSensorInfo.addSensorData(event.values[0], event.values[1], event.values[2], 0, (long)(event.timestamp / 1e6));
                count[0] = 0;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener linearAccListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            // add complete
            completeSensorInfo.addSensorData(event.values[0], event.values[1], event.values[2], 1, (long)(event.timestamp / 1e6));
            // add sampled
            count[1]++;
            if (count[1] >= 10) {
                sampledSensorInfo.addSensorData(event.values[0], event.values[1], event.values[2], 1, (long)(event.timestamp / 1e6));
                count[1] = 0;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    private SensorEventListener accListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            System.arraycopy(event.values, 0, accMark, 0, accMark.length);
            updateOrientationAngles(event.timestamp);
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

    public void updateOrientationAngles(long timestamp) {
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

    private void stopService() {
        // 停止定位服务
        mLocationClient.stopLocation();
        if (sensorManager != null) {
            sensorManager.unregisterListener(gyroListener);
            sensorManager.unregisterListener(linearAccListener);
            sensorManager.unregisterListener(accListener);
            sensorManager.unregisterListener(magListener);
        }
    }

    private void collectData() {
//        new WeatherInfo().collectData(); // 收集天气数据，并附加在WeatherInfo.json中
//        new LocationInfo().collectData(); // 收集位置数据，并附加在LocationInfo.json中
        completeSensorInfo.collectData();
        sampledSensorInfo.collectData();
    }
}