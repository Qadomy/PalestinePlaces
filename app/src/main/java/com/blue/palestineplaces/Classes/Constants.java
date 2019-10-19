package com.blue.palestineplaces.Classes;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

public final class Constants {

    private Constants() {
    }


    public static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    //public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km
    public static final float GEOFENCE_RADIUS_IN_METERS = 500.0f; // 1 mile, 1.6 km

    /**
     * Map for storing information about airports in the San Francisco bay area.
     */

    public static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<String, LatLng>();
    static {

        BAY_AREA_LANDMARKS.put("BLUE COMPANY", new LatLng(31.905446, 35.211472));
        BAY_AREA_LANDMARKS.put("دوار المنارة", new LatLng(31.904948, 35.204439));
        BAY_AREA_LANDMARKS.put("Home", new LatLng(32.288742, 35.038921));
        BAY_AREA_LANDMARKS.put("كازية حسونة", new LatLng(32.320124, 35.042970));
        BAY_AREA_LANDMARKS.put("نور شمس", new LatLng(32.319737, 35.054051));
        BAY_AREA_LANDMARKS.put("بلعا", new LatLng(32.330941, 35.111157));
        BAY_AREA_LANDMARKS.put("عنبتا", new LatLng(32.305236, 35.121386));
        BAY_AREA_LANDMARKS.put("كدوميم", new LatLng(32.217673, 35.177242));
        BAY_AREA_LANDMARKS.put("حوارة", new LatLng(32.152914, 35.259108));
        BAY_AREA_LANDMARKS.put("زعترة", new LatLng(32.121407, 35.257147));
        BAY_AREA_LANDMARKS.put("شارع البيرة", new LatLng(31.913266, 35.215829));
        BAY_AREA_LANDMARKS.put("كازية الهدى", new LatLng(31.922862, 35.219852));
//        BAY_AREA_LANDMARKS.put("سنجل", new LatLng(32.032800, 35.271455));
//        BAY_AREA_LANDMARKS.put("شارع السفارة الهولندية", new LatLng(31.912516, 35.215983));


    }

}
