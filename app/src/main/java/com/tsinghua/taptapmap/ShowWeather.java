package com.tsinghua.taptapmap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;

import java.util.ArrayList;
import java.util.List;

public class ShowWeather extends Activity implements WeatherSearch.OnWeatherSearchListener {
    private Button buttonQuery;
    private TextView tvCity;
    private TextView tvWeatherLive;
    private TextView tvWeatherForecast;
    private AMapLocationClient mLocationClient;
    private String mCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_weather);

        buttonQuery = findViewById(R.id.button_query);
        tvCity = findViewById(R.id.tv_city);
        tvWeatherLive = findViewById(R.id.tv_weather_live);
        tvWeatherForecast = findViewById(R.id.tv_weather_forecast);

        buttonQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: 自动确定城市
                WeatherSearchQuery queryLive = new WeatherSearchQuery(mCity, WeatherSearchQuery.WEATHER_TYPE_LIVE);
                WeatherSearch weatherSearchLive = new WeatherSearch(ShowWeather.this);
                weatherSearchLive.setOnWeatherSearchListener(ShowWeather.this);
                weatherSearchLive.setQuery(queryLive);
                weatherSearchLive.searchWeatherAsyn();

                WeatherSearchQuery queryForecast = new WeatherSearchQuery(mCity, WeatherSearchQuery.WEATHER_TYPE_FORECAST);
                WeatherSearch weatherSearchForecast = new WeatherSearch(ShowWeather.this);
                weatherSearchForecast.setOnWeatherSearchListener(ShowWeather.this);
                weatherSearchForecast.setQuery(queryForecast);
                weatherSearchForecast.searchWeatherAsyn();
            }
        });

        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving); // Just for city query
        mLocationOption.setInterval(2000);
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                if (aMapLocation.getCity() != null && !aMapLocation.getCity().isEmpty()) {
                    mCity = aMapLocation.getCity();
                    StringBuffer sb = new StringBuffer(256);
                    sb.append(mCity + '\n');
                    sb.append(aMapLocation.getPoiName() + '|' + aMapLocation.getStreet() + '\n');
                    tvCity.setText(sb.toString());
                }
            }
        });
    }

    @Override
    public void onWeatherLiveSearched(LocalWeatherLiveResult result, int rcode) {
        if (rcode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getLiveResult() != null) {
                StringBuffer sb = new StringBuffer(256);
                LocalWeatherLive weatherlive = result.getLiveResult();
                sb.append("Update time: " + weatherlive.getReportTime() + '\n');
                sb.append("Weather: " + weatherlive.getWeather() + '\n');
                sb.append("Temperature: " + weatherlive.getTemperature() + "°" + '\n');
                sb.append("Wind: " + weatherlive.getWindDirection() + "风" + weatherlive.getWindPower() + "级" + '\n');
                sb.append("Humidity: " + weatherlive.getHumidity() + "%" + '\n');
                tvWeatherLive.setText(sb.toString());
            } else {
                Toast.makeText(ShowWeather.this, "查询无结果", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ShowWeather.this, "查询出错", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onWeatherForecastSearched(LocalWeatherForecastResult result, int rcode) {
        if (rcode == AMapException.CODE_AMAP_SUCCESS) {
            if (result != null && result.getForecastResult() != null) {
                StringBuffer sb = new StringBuffer(256);
                LocalWeatherForecast weatherForecast = result.getForecastResult();
                sb.append("Update time: " + weatherForecast.getReportTime() + '\n');
                List<LocalDayWeatherForecast> dayWeatherList = weatherForecast.getWeatherForecast();
                for (LocalDayWeatherForecast dayWeather : dayWeatherList) {
                    sb.append(dayWeather.getDate() + "|" + dayWeather.getDayWeather() + "|" + dayWeather.getDayTemp() + "°|" +
                            dayWeather.getDayWindDirection() + "风" + dayWeather.getDayWindPower() + "级" + '\n');
                }
                tvWeatherForecast.setText(sb.toString());
            } else {
                Toast.makeText(ShowWeather.this, "查询无结果", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ShowWeather.this, "查询出错", Toast.LENGTH_SHORT).show();
        }
    }
}