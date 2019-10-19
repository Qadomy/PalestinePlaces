package com.blue.palestineplaces.Classes;

import com.google.android.gms.maps.model.LatLng;

public class Location {

    private LatLng locationPoistion;
    private String locationName;


    public Location(LatLng locationPoistion, String locationName) {
        this.locationPoistion = locationPoistion;
        this.locationName = locationName;
    }

    public LatLng getLocationPoistion() {
        return locationPoistion;
    }

    public void setLocationPoistion(LatLng locationPoistion) {
        this.locationPoistion = locationPoistion;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
}
