package com.tsinghua.taptapmap.collect.data;

import java.util.ArrayList;
import java.util.List;

public class SensorData {
    private List<List<Float>> data = new ArrayList<>();

    public List<List<Float>> getData() {
        return data;
    }

    public void setData(List<List<Float>> data) {
        this.data = data;
    }

    public void insert(List<Float> d, int limit) {
        while (data.size() >= limit) {
            data.remove(0);
        }
        data.add(d);
    }

    public void clear() {
        data.clear();
    }
}
