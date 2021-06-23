package com.tsinghua.taptapmap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

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

    public class SystemInfo extends Info{
        //这个类由苏夏构建，包括了屏幕亮度，环境亮度，气压等属性，也包括了wifi以及蓝牙的信息
        private int screenBrightness;
        private float environmentBrightness;
        private float airPressure;
        private List<WiFi_Info> wiFiInfoList;
        private WiFi_Info connectedWiFi;
        private List<Bluetooth_Info> bluetoothInfoList;
        SensorManager sm;
        WifiManager wifiManager;
        BluetoothManager bluetoothManager;

        public SystemInfo() {
            super();
            sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            wifiManager= (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            bluetoothManager= (BluetoothManager) getApplicationContext().getSystemService(BLUETOOTH_SERVICE);
            wiFiInfoList=new ArrayList<>();
            bluetoothInfoList=new ArrayList<>();
        }

        public class WiFi_Info
        {
            String BSSID,SSID,capabilities;
            int centerFreq0,centerFreq1,channelWidth,frequency,level;//The unused attributes are for newer version of Android
            boolean isNew,isConnected;
            public WiFi_Info(ScanResult result, boolean isNewScanResult, boolean isConnect)
            {
                BSSID=result.BSSID;
                SSID=result.SSID;
                if(BSSID==null)
                    BSSID="NULL";
                if(SSID==null)
                    SSID="NULL";
                capabilities=result.capabilities;
                frequency=result.frequency;
                level=result.level;
                isNew=isNewScanResult;
                isConnected=isConnect;

            }
            public WiFi_Info(WifiInfo info, boolean isNewScanResult, boolean isConnect)
            {

                BSSID=info.getBSSID();
                if(BSSID==null)
                    BSSID="NULL";
                SSID=info.getSSID();
                if(SSID==null)
                    SSID="NULL";
                capabilities="NULL";
                frequency=info.getFrequency();
                level=0;
                isNew=isNewScanResult;
                isConnected=isConnect;
            }
            public JSONObject getJsonObject() throws JSONException {
                JSONObject obj=new JSONObject();
                obj.put("BSSID",BSSID);
                obj.put("SSID or Name",SSID);
                obj.put("capabilities",capabilities);
                obj.put("frequency",frequency);
                obj.put("level",level);
                obj.put("isNew",isNew);
                obj.put("isConnected",isConnected);
                return obj;
            }
        }
        public class Bluetooth_Info {
            String name;
            String macAddress;
            boolean linked;
            int bondState;
            int type;
            BluetoothClass btClass;

            public Bluetooth_Info(BluetoothDevice device, boolean isLinked) {
                String deviceName = device.getName();
                //if(deviceName==null)
                //deviceName="NULL";
                String deviceMacAddress = device.getAddress(); // MAC address
                BluetoothClass deviceClass = device.getBluetoothClass();
                int deviceBondState = device.getBondState();
                int deviceType = device.getType();
                name = deviceName;
                if (name == null)
                    name = "NULL";
                macAddress = deviceMacAddress;
                if (macAddress == null)
                    macAddress = "NULL";
                linked = isLinked;
                bondState = deviceBondState;
                type = deviceType;
                btClass = deviceClass;
            }

            public JSONObject getJsonObject() throws JSONException {
                JSONObject obj = new JSONObject();
                obj.put("deviceName", name);
                obj.put("macAddress", macAddress);
                obj.put("ifDeviceIsLinked", linked);
                obj.put("bondState", bondState);
                obj.put("deviceType", type);
                obj.put("deviceClass", btClass);
                return obj;
            }
        }
        @Override
        public void collectData() {
            //设置listener和receiver
            XiaSensorEventListener sensorEventListenerPressure=new XiaSensorEventListener();
            XiaSensorEventListener sensorEventListenerLight=new XiaSensorEventListener();
            Sensor pressure= sm.getDefaultSensor(Sensor.TYPE_PRESSURE);
            Sensor light= sm.getDefaultSensor(Sensor.TYPE_LIGHT);
            sm.registerListener(sensorEventListenerPressure, pressure, SensorManager.SENSOR_DELAY_NORMAL);
            sm.registerListener(sensorEventListenerLight, light, SensorManager.SENSOR_DELAY_NORMAL);
            IntentFilter wifiFilter = new IntentFilter();
            wifiFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
            registerReceiver(wifiScanReceiver, wifiFilter);
            IntentFilter bluetoothFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(bluetoothReceiver, bluetoothFilter);
            //Collect other system info. Here only screen brightness
            screenBrightness = Settings.System.getInt(getContentResolver(),Settings.System.SCREEN_BRIGHTNESS,125);
            //scan for wifi and bt
            WifiInfo info= wifiManager.getConnectionInfo();
            setConnectedWifi(info);
            boolean started=wifiManager.startScan();
            Log.d("wifi scanner", String.valueOf(started));
            if(started==false)
            {
                scanFailure();
            }
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            //获取已配对的蓝牙设备,如果有的话则直接存储，如果没有已经配对的设备则进行发现
            Set<BluetoothDevice> pairedDevices= bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {//有已经连接的设备

                for (BluetoothDevice device : pairedDevices) {
                    //String deviceName = device.getName();
                    //String deviceHardwareAddress = device.getAddress();
                    addBluetooth(device,true);
                }
            }
            else//没有已经连接的设备，进行设备发现
            {
                started= bluetoothAdapter.startDiscovery();
            }
            //10秒后存储数据
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Save();
                        //取消所有listener和receiver
                        sm.unregisterListener(sensorEventListenerPressure);
                        sm.unregisterListener(sensorEventListenerLight);
                        unregisterReceiver(bluetoothReceiver);
                        unregisterReceiver(wifiScanReceiver);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },10000);

        }
        protected void Save() throws JSONException {
            //First get the json
            JSONObject obj=new JSONObject();
            responseTime = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS").format(new Date());
            obj.put("responseTime",responseTime);
            obj.put("queryTime",queryTime);
            obj.put("screenBrightness",screenBrightness);
            obj.put("environmentBrightness",environmentBrightness);
            obj.put("airPressure",airPressure);
            JSONArray wifiArray=new JSONArray();
            for (WiFi_Info info:wiFiInfoList) {
                wifiArray.put(info.getJsonObject());
            }
            obj.put("wifiList",wifiArray);
            JSONArray btArray=new JSONArray();
            for (Bluetooth_Info info:bluetoothInfoList) {
                btArray.put(info.getJsonObject());
            }
            obj.put("bluetoothList",btArray);
            String filePath = "/storage/emulated/0/";
            String fileName = getClass().getName() + ".json";
            String result = obj.toString();
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

        private final class XiaSensorEventListener implements SensorEventListener
        {
            //可以得到传感器实时测量出来的变化值
            @Override
            public void onSensorChanged(SensorEvent event)
            {
                if(event.sensor.getType()== Sensor.TYPE_LIGHT)
                {
                    environmentBrightness=event.values[0];
                }
                else if(event.sensor.getType()==Sensor.TYPE_PRESSURE)
                {
                    airPressure=event.values[0];
                }

            }
            //重写变化
            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy)
            {
            }
        }
        private final BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };
        private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //Update bluetooth list
                    addBluetooth(device,false);
                }
            }
        };
        private void scanSuccess() {
            List<ScanResult> results = wifiManager.getScanResults();
            //存储一下现在的状态
            addWiFi(results,true);
        }

        private void scanFailure() {
            // handle failure: new scan did NOT succeed
            // consider using old scan results: these are the OLD results!
            List<ScanResult> results = wifiManager.getScanResults();
            addWiFi(results,false);
        }
        public void addWiFi(List<ScanResult> results,boolean isNew)
        {
            //This function receives a list of wifi scan results, then add them to list one by one. Notice that a relatively strict repetition filter is adopted.
            for (ScanResult r:results)
            {
                boolean connected=false;
                if(connectedWiFi!=null)
                {
                    if(r.BSSID.equals(connectedWiFi.BSSID) && r.SSID.equals(connectedWiFi.SSID))
                        connected=true;
                }
                WiFi_Info info=new WiFi_Info(r,isNew,connected);
                //去重
                boolean existed=false;
                for (WiFi_Info i:wiFiInfoList) {
                    if(i.SSID.equals(info.SSID) && i.BSSID.equals(info.BSSID))
                    {
                        existed=true;
                    }
                }
                if(existed==false)
                {
                    wiFiInfoList.add(info);
                }
            }

        }
        public void setConnectedWifi(WifiInfo info)
        {
            WiFi_Info inf=new WiFi_Info(info,false,true);
            connectedWiFi=inf;
        }
        public void addBluetooth(BluetoothDevice device, boolean linked)
        {
            Bluetooth_Info info=new Bluetooth_Info(device,linked);
            //去重
            boolean existed=false;
            for (Bluetooth_Info i:bluetoothInfoList) {
                if(i.name.equals(info.name) && i.macAddress.equals(info.macAddress))
                {
                    existed=true;
                }
            }
            if(existed==false)
            {
                bluetoothInfoList.add(info);
                Log.d("bth scanner","found one");
            }

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
        new SystemInfo().collectData();
    }
}