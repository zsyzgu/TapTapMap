package com.tsinghua.taptapmap.collect.data;

import android.util.Log;

public class SingleBluetoothData {
    private String name;
    private String address;
    private int bondState;
    private int type;
    private int deviceClass;
    private int majorDeviceClass;

    public SingleBluetoothData(String name, String address,
                               int bondState, int type,
                               int deviceClass, int majorDeviceClass) {
        if (name == null) {
            this.name = "NULL";
        } else {
            this.name = name;
        }
        if (address == null) {
            this.address = "NULL";
        } else {
            this.address = address;
        }
        this.bondState = bondState;
        this.type = type;
        this.deviceClass = deviceClass;
        this.majorDeviceClass = majorDeviceClass;
    }

    public int getBondState() {
        return bondState;
    }

    public int getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public int getDeviceClass() {
        return deviceClass;
    }

    public int getMajorDeviceClass() {
        return majorDeviceClass;
    }

    public void setAddress(String address) {
        if (address == null) {
            this.address = "NULL";
        } else {
            this.address = address;
        }
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }

    public void setName(String name) {
        if (name == null) {
            this.name = "NULL";
        } else {
            this.name = name;
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setDeviceClass(int deviceClass) {
        this.deviceClass = deviceClass;
    }

    public void setMajorDeviceClass(int majorDeviceClass) {
        this.majorDeviceClass = majorDeviceClass;
    }
}
