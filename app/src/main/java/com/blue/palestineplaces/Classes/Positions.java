package com.blue.palestineplaces.Classes;

import android.media.MediaPlayer;

import com.google.android.gms.maps.model.LatLng;

public class Positions {

    private String locationName;
    private LatLng locationLatLng;
    private MediaPlayer mediaPlayer;


    public Positions(String locationName, LatLng locationLatLng, MediaPlayer mediaPlayer) {
        this.locationName = locationName;
        this.locationLatLng = locationLatLng;
        this.mediaPlayer = mediaPlayer;
    }

    public LatLng getLocationLatLng() {
        return locationLatLng;
    }

    public void setLocationLatLng(LatLng locationLatLng) {
        this.locationLatLng = locationLatLng;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }
}
