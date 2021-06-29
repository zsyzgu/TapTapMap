package com.tsinghua.taptapmap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tsinghua.taptapmap.collect.collector.BluetoothCollector;
import com.tsinghua.taptapmap.collect.collector.CompleteIMUCollector;
import com.tsinghua.taptapmap.collect.collector.LocationClient;
import com.tsinghua.taptapmap.collect.collector.LocationCollector;
import com.tsinghua.taptapmap.collect.collector.NonIMUCollector;
import com.tsinghua.taptapmap.collect.collector.SampledIMUCollector;
import com.tsinghua.taptapmap.collect.collector.WifiCollector;
import com.tsinghua.taptapmap.collect.data.NonIMUData;
import com.tsinghua.taptapmap.collect.trigger.ClickTrigger;
import com.tsinghua.taptapmap.collect.trigger.TimerTrigger;
import com.tsinghua.taptapmap.collect.trigger.Trigger;

public class DataCollection extends Activity {
    private Button mButtonDataCollection;
    private TextView mTvDataCollection;

    ClickTrigger clickTrigger;
    TimerTrigger timerTrigger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_collection);
        mButtonDataCollection = findViewById(R.id.button_data_collection);
        mTvDataCollection = findViewById(R.id.tv_data_collection);
        mButtonDataCollection.setOnClickListener(v -> {
            mTvDataCollection.setText("");
            collectData();
        });
        initService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService();
    }

    private void initService() {
        clickTrigger = new ClickTrigger(getApplicationContext(), Trigger.CollectorType.Wifi);
        timerTrigger = new TimerTrigger(this, Trigger.CollectorType.All);
        timerTrigger.trigger();
    }

    private void stopService() {
    }

    private void collectData() {
        clickTrigger.trigger();
    }
}