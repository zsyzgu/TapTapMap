package com.tsinghua.taptapmap.collect.collector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.tsinghua.taptapmap.collect.data.SingleWifiData;
import com.tsinghua.taptapmap.collect.data.WifiData;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiCollector extends Collector {

    private WifiManager wifiManager;

    private WifiData data;

    public WifiCollector(Context context) {
        super(context);
        this.data = new WifiData();
    }

    @Override
    public void initialize() {
        wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {
                    List<ScanResult> results = wifiManager.getScanResults();
                    for (ScanResult result: results) {
                        synchronized (this) {
                            data.insert(new SingleWifiData(result.SSID, result.BSSID,
                                    result.capabilities,
                                    result.level, result.frequency,
                                    result.timestamp,
                                    result.channelWidth,
                                    result.centerFreq0, result.centerFreq1));
                        }
                    }
                }
            }
        }, wifiFilter);
    }

    @Override
    public synchronized void collect() {
        data.clear();
        wifiManager.startScan();
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (this) {
                    saver.save(data);
                }
            }
        }, 10000);
    }

    @Override
    public void close() {

    }

    @Override
    protected String getSaveFolderName() {
        return "Wifi";
    }
}
