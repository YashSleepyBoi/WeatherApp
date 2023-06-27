package com.example.test_app;

import java.util.ArrayList;

public class WeatherList {

    private static ArrayList<Object> WeatherList;
    private static WeatherList init=null;

    WeatherList(){
        if(init==null){
            init=this;
        }
    }
    public void setWeatherList(Object o){
        WeatherList.add(o);
    }

    public static ArrayList<Object> getWeatherList() {
        return WeatherList;
    }
}
