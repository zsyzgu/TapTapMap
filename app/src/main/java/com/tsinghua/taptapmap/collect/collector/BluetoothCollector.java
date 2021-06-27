package com.tsinghua.taptapmap.collect.collector;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.tsinghua.taptapmap.collect.data.BluetoothData;
import com.tsinghua.taptapmap.collect.data.SingleBluetoothData;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class BluetoothCollector extends Collector {

    private BluetoothData data;

    public BluetoothCollector(Context context) {
        super(context);
        data = new BluetoothData();
    }

    private synchronized void insert(BluetoothDevice device) {
        data.insert(new SingleBluetoothData(device.getName(), device.getAddress(),
                device.getBondState(), device.getType(),
                device.getBluetoothClass().getDeviceClass(),
                device.getBluetoothClass().getMajorDeviceClass()));
    }

    @Override
    public void initialize() {
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BluetoothDevice.ACTION_FOUND)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    insert(device);
                }
            }
        }, bluetoothFilter);
    }

    @Override
    public synchronized void collect() {
        data.clear();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device: pairedDevices) {
                insert(device);
            }
        }

        bluetoothAdapter.startDiscovery();
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
        return "Bluetooth";
    }
}
