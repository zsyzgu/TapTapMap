package com.tsinghua.taptapmap;

import android.app.Activity;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tsinghua.taptapmap.collect.collector.CompleteSensorCollector;
import com.tsinghua.taptapmap.collect.collector.LocationCollector;
import com.tsinghua.taptapmap.collect.collector.SampledSensorCollector;

public class DataCollection extends Activity {
    private Button mButtonDataCollection;
    private TextView mTvDataCollection;

    // 传感器数据收集相关
    private CompleteSensorCollector completeSensorInfo;
    private SampledSensorCollector sampledSensorInfo;
    private LocationCollector locationCollector;

    private SensorManager sensorManager;

    private final int samplingPeriod = 10000;

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
        completeSensorInfo = new CompleteSensorCollector(this, samplingPeriod, 1);
        completeSensorInfo.initialize();
        sampledSensorInfo = new SampledSensorCollector(this, samplingPeriod, 10);
        sampledSensorInfo.initialize();
        locationCollector = new LocationCollector(this);
        locationCollector.initialize();
    }

    private void stopService() {
    }

    private void collectData() {
        // completeSensorInfo.collect();
        // sampledSensorInfo.collect();
        locationCollector.collect();
    }
}