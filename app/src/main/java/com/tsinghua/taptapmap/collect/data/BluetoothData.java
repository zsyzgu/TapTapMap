package com.tsinghua.taptapmap.collect.data;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BluetoothData {
    private List<SingleBluetoothData> data = new ArrayList<>();

    public void setData(List<SingleBluetoothData> data) {
        this.data = data;
    }

    public List<SingleBluetoothData> getData() {
        return data;
    }

    public void clear() {
        this.data.clear();
    }

    public void insert(SingleBluetoothData single) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getName().equals(single.getName()) && data.get(i).getLinked() == single.getLinked()) {
                data.set(i, single);
                return;
            }
        }
        data.add(single);
    }
}
