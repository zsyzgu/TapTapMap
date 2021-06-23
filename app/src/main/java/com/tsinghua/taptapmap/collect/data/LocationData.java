package com.tsinghua.taptapmap.collect.data;

public class LocationData {
    double longitude;
    double latitude;
    double altitude;
    float accuracy;
    String floor;
    String city;
    String poiName;
    String street;

    public LocationData(double longitude, double latitude, double altitude,
                 float accuracy,
                 String floor, String city, String poiName, String street) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
        this.accuracy = accuracy;
        this.floor = floor;
        this.city = city;
        this.poiName = poiName;
        this.street = street;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public String getCity() {
        return city;
    }

    public String getFloor() {
        return floor;
    }

    public String getPoiName() {
        return poiName;
    }

    public String getStreet() {
        return street;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPoiName(String poiName) {
        this.poiName = poiName;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
