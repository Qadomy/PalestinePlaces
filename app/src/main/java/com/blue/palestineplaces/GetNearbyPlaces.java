package com.blue.palestineplaces;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;

import java.net.MalformedURLException;
import java.net.URL;

public class GetNearbyPlaces extends AsyncTask<Object, String, String> {


    GoogleMap map;
    String url;

    @Override
    protected String doInBackground(Object... params) {

        map = (GoogleMap)params[0];
        url = (String)params[1];

        try {

            URL myUrl = new URL(url);
             

        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        return null;
    }
}
