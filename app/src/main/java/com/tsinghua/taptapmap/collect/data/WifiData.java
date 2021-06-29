package com.tsinghua.taptapmap.collect.data;

import java.util.ArrayList;
import java.util.List;

public class WifiData {
    private List<SingleWifiData> data = new ArrayList<>();

    public List<SingleWifiData> getData() {
        return data;
    }

    public void setData(List<SingleWifiData> data) {
        this.data = data;
    }

    public void clear() {
        data.clear();
    }

    public void insert(SingleWifiData single) {
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getBssid().equals(single.getBssid()) && data.get(i).getConnected() == single.getConnected()) {
                data.set(i, single);
                return;
            }
        }
        data.add(single);
    }
}
