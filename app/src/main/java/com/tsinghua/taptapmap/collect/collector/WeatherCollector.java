package com.tsinghua.taptapmap.collect.collector;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.tsinghua.taptapmap.collect.data.WeatherData;

public class WeatherCollector extends Collector {

    private LocationClient client;

    private WeatherData data;

    public WeatherCollector(Context context, String triggerFolder) {
        super(context, triggerFolder);
    }

    @Override
    public void initialize() {
        client = LocationClient.getInstance(mContext);
    }

    @Override
    public synchronized void collect() {
        if (client.mLocation != null && !client.mLocation.getCity().equals("")) {
            AMapLocation mLocation = client.mLocation.clone();
            WeatherSearchQuery queryLive = new WeatherSearchQuery(mLocation.getCity(), WeatherSearchQuery.WEATHER_TYPE_LIVE);
            WeatherSearch weatherSearchLive = new WeatherSearch(mContext);
            weatherSearchLive.setOnWeatherSearchListener(new WeatherSearch.OnWeatherSearchListener() {
                @Override
                public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int i) {
                    LocalWeatherLive result = localWeatherLiveResult.getLiveResult();
                    data = new WeatherData(
                            result.getReportTime(),
                            result.getWeather(),
                            result.getWindDirection(),
                            result.getWindPower(),
                            Float.parseFloat(result.getTemperature()),
                            Float.parseFloat(result.getHumidity())
                    );
                    saver.save(data);
                }

                @Override
                public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i) {

                }
            });
            weatherSearchLive.setQuery(queryLive);
            weatherSearchLive.searchWeatherAsyn();
        }
    }

    @Override
    public void close() {

    }

    @Override
    protected String getSaveFolderName() {
        return "Weather";
    }
}
