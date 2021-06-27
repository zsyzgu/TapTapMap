package com.tsinghua.taptapmap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tsinghua.taptapmap.collect.collector.BluetoothCollector;
import com.tsinghua.taptapmap.collect.collector.CompleteIMUCollector;
import com.tsinghua.taptapmap.collect.collector.LocationCollector;
import com.tsinghua.taptapmap.collect.collector.NonIMUCollector;
import com.tsinghua.taptapmap.collect.collector.SampledIMUCollector;
import com.tsinghua.taptapmap.collect.collector.WifiCollector;
import com.tsinghua.taptapmap.collect.data.NonIMUData;
import com.tsinghua.taptapmap.collect.trigger.Trigger;

public class DataCollection extends Activity {
    private Button mButtonDataCollection;
    private TextView mTvDataCollection;

    // 传感器数据收集相关
    private CompleteIMUCollector completeIMUCollector;
    /*
    private SampledIMUCollector sampledIMUCollector;
    private LocationCollector locationCollector;

    private WifiCollector wifiCollector;
    private BluetoothCollector bluetoothCollector;

    private NonIMUCollector nonIMUCollector;

    private final int samplingPeriod = 10000;
     */
    Trigger trigger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);
        mButtonDataCollection = findViewById(R.id.button_data_collection);
        mTvDataCollection = findViewById(R.id.tv_data_collection);
        initService();

        mButtonDataCollection.setOnClickListener(v -> {
            mTvDataCollection.setText("");
            collectData();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
    }

    private void initService() {
        trigger = new Trigger(this, Trigger.CollectorType.CompleteIMU);


        // completeIMUCollector = new CompleteIMUCollector(this, 10000, 1);
    }

    private void stopService() {
    }

    private void collectData() {
        trigger.trigger();
        // completeIMUCollector.collect();
        // nonIMUCollector.collect();
    }
}