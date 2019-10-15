package com.blue.palestineplaces.Classes;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;

public class GetNearbyPlacesData extends AsyncTask<Object, String, String> {


    String googleLacesData;
    GoogleMap map;
    String url;

    @Override
    protected String doInBackground(Object... objects) {
        map = (GoogleMap)objects[0];
        url = (String)objects[1];


        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
