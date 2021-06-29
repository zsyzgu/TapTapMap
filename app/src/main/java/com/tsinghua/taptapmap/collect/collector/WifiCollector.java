package com.tsinghua.taptapmap.collect.collector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
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

    public WifiCollector(Context context, String triggerFolder) {
        super(context, triggerFolder);
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
                                    result.centerFreq0, result.centerFreq1, false));
                        }
                    }
                }
            }
        }, wifiFilter);
    }

    @Override
    public synchronized void collect() {
        data.clear();
        WifiInfo info = wifiManager.getConnectionInfo();
        if (info != null) {
            data.insert(new SingleWifiData(info.getSSID(), info.getBSSID(),
                    "NULL",
                    0, info.getFrequency(),
                    System.currentTimeMillis() * 1000,
                    0,
                    0, 0, true));
        }
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
