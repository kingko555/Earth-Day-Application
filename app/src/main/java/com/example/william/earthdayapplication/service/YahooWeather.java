package com.example.william.earthdayapplication.service;

import android.net.Uri;
import android.os.AsyncTask;

import com.example.william.earthdayapplication.data.Channel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Collooso on 4/22/2017.
 */

public class YahooWeather {
    private WeatherCB callback;
    private String location;
    private Exception error;

    public void setLocation(String location) {
        this.location = location;
    }



    public YahooWeather(WeatherCB callback){
        this.callback = callback;
    }

    public void refreshWeather(final String location){
        new AsyncTask<String, Void, String>(){

            @Override
            protected String doInBackground(String... params) {
                String YQL = String.format("select * from weather.forecast where woeid in (select woeid from geo.places(1) where text=\"%s\")", location);

                String endpoint = String.format("https://query.yahooapis.com/v1/public/yql?q=&format=json", Uri.encode(YQL));


                try {
                    URL url = new URL(endpoint);
                    URLConnection connection = url.openConnection();
                    InputStream inputStream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder result = new StringBuilder();
                    String line;

                    while((line = reader.readLine()) != null){
                        result.append(line);

                    }

                    return result.toString();

                } catch (Exception e) {
                    error = e;

                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                if(s == null && error != null){
                    callback.serviceFail(error);
                    return;

                }

                try {
                    JSONObject data = new JSONObject(s);

                    JSONObject queryResults = new JSONObject("query");

                    int count = queryResults.optInt("count");
                    if(count == 0){
                        callback.serviceFail(new LocationWeatherException("No weather information founds " + location));
                        return;
                    }

                    Channel channel = new Channel();
                    channel.populate(queryResults.optJSONObject("Results").optJSONObject("channel"));

                    callback.servicePass(channel);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


        }.execute();
    }

    public class LocationWeatherException extends Exception{
        public LocationWeatherException(String detailMessage){
            super(detailMessage);
        }
    }
}
