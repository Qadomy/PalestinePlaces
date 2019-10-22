package com.blue.palestineplaces.Classes;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class Location {

    private LatLng locationPoistion;
    private String locationName;
    private String locationDescription;



    public Location(LatLng locationPoistion, String locationName, String locationDescription) {
        this.locationPoistion = locationPoistion;
        this.locationName = locationName;
        this.locationDescription = locationDescription;
    }

    public LatLng getLocationPoistion() {
        return locationPoistion;
    }


    public String getLocationName() {
        return locationName;
    }

    public String getLocationDescription() {
        return locationDescription;
    }
}
