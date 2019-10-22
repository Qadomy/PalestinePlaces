package com.blue.palestineplaces.Classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

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


    public String getLocationName() {
        return locationName;
    }

}
