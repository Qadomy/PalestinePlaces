package com.blue.palestineplaces;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.blue.palestineplaces.Classes.Constants;
import com.blue.palestineplaces.Classes.GeofenceTrasitionService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status> {


    private static final String tAG = MapsActivity.class.getSimpleName();

    private GeofencingClient geofencingClient;

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private GoogleMap mMap;

    private Location lastlocation;

    private Marker locationMarker, geoFenceMarker;

    private Circle geoFenceLimits;

    private ArrayList<Geofence> mGeofenceList;

    private static final int REQ_PERMISSION = 1337;

    // These numbers in mili seconds in extremely low, and should be used only for debug
    private final int UPDATE_INTERVAL =  3 * 60 * 1000; // 3 minutes
    private final int FASTEST_INTERVAL = 3 * 60 * 1000; // 3 minutes


    // For creating GeoFence
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 500.0f; // in meters

    // For PendingIntent
    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    // notifications
    private static final String NOTIFICATION_MSG = "NOTIFICATION MSG";


    @Override
    protected void onStart() {
        // Call GoogleApiClient connection when starting the Activity
        super.onStart();
        client.connect();

    }

    /*
    *
    * onCreate
    *
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        geofencingClient = LocationServices.getGeofencingClient(this);

        // init the array list
        mGeofenceList = new ArrayList<Geofence>();


        // init the geofencingClient
        //GeofencingClient geofencingClient = LocationServices.getGeofencingClient(this);

        createGoogleApi();

    }// end of onCreate ..

    @Override
    protected void onStop() {
        // Disconnect GoogleApiClient when stopping Activity
        super.onStop();
        client.disconnect();
    }

    // here to populate the geofence place list, called after the map ready
    private void populateGeofenceList(){

        for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {
            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this geofence
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.

                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )


                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time. // TODO: check here for expiration
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)


                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)


                    // Create the geofence.
                    .build()
            );

            String id = entry.getKey();
            Toast.makeText(this, id, Toast.LENGTH_SHORT).show();

            LatLng latLng = new LatLng(entry.getValue().latitude ,entry.getValue().longitude);
            CircleOptions circleOptions = new CircleOptions()
                    .center(latLng)
                    .strokeColor(Color.argb(50, 70,70,70))
                    .fillColor(Color.argb(100, 150,150,150))
                    .radius(GEOFENCE_RADIUS);

            mMap.addCircle(circleOptions);



            String title = entry.getKey();

            // define marker option
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

            mMap.addMarker(markerOptions);

            try {
                geofencingClient.addGeofences(getGeofencingRequest(),
                        createGeofencePendingIntent()).addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
            } catch (SecurityException se) {
                // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
                Toast.makeText(this, se.getMessage(), Toast.LENGTH_SHORT).show();
            }



        }
    }// end of populateGeofenceList

    // Create GoogleApiClient instance
    public void createGoogleApi(){
        Log.d(tAG, "createGoogleApi()");
        if ( client == null ) {
            client = new GoogleApiClient.Builder( this )
                    .addConnectionCallbacks( this )
                    .addOnConnectionFailedListener( this )
                    .addApi( LocationServices.API )
                    .build();
        }
    }


    // OnMapReadyCallback interface
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(tAG, "onMapReady()");

        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);


        populateGeofenceList();

    }


    // LocationListener interface
    @Override
    public void onLocationChanged(Location location) {
        Log.d(tAG, "onLocationChanged ["+location+"]");

        lastlocation = location;
        writeActualLocation(location);


    }

    // Write location coordinates on UI
    private void writeActualLocation(Location location) {
        markerLocation(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    // create location marker
    private void markerLocation(LatLng latLng){
        Log.i(tAG, "markerLocation("+latLng+")");
        String title = latLng.latitude + ", " + latLng.longitude;
        MarkerOptions markerOptions =  new MarkerOptions()
                .position(latLng)
                .title(title);

        if (mMap != null){
            // Remove the anterior marker
            if (locationMarker != null){
                locationMarker.remove();
            }

            locationMarker = mMap.addMarker(markerOptions);
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14f);
            mMap.animateCamera(cameraUpdate);
        }
    }


    // GoogleApiClient.ConnectionCallbacks interface
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(tAG, "onConnected()");

        getLastKnownLocation();
    }

    // Get last known location
    private void getLastKnownLocation(){
        Log.d(tAG, "getLastKnownLocation()");

        if (checkPermission()){
            lastlocation = LocationServices.FusedLocationApi.getLastLocation(client);
            if (lastlocation != null){
                Log.i(tAG, "LasKnown location. " +
                        "Long: " + lastlocation.getLongitude() +
                        " | Lat: " + lastlocation.getLatitude());

                writeLastLocation();
                startLocationUpdates();
            }else {
                Log.w(tAG, "No location retrieved yet");
                startLocationUpdates();
            }

        }else{
            askPermission();
        }
    }

    // start location update
    private void startLocationUpdates() {
        Log.i(tAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        if (checkPermission()){
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    private void writeLastLocation() {
        writeActualLocation(lastlocation);
    }


    // GoogleApiClient.ConnectionCallbacks interface
    @Override
    public void onConnectionSuspended(int i) {
        Log.w(tAG, "onConnectionSuspended()");


    }


    // GoogleApiClient.OnConnectionFailedListener interface
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(tAG, "onConnectionFailed()");

    }

    // called when click on any part of map
    // GoogleMap.OnMapClickListener interface
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(tAG, "onMapClick("+latLng +")");

        //markerForGeofence(latLng);

    }



    // called when Marker is touched
    // GoogleMap.OnMarkerClickListener interface
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(tAG, "onMarkerClickListener: " + marker.getPosition() );
        return false;
    }


    // ResultCallback
    @Override
    public void onResult(@NonNull Status status) {
        Log.i(tAG, "onResult: " + status);


        if (status.isSuccess()) {
            Toast.makeText(this, "Geofences Added", Toast.LENGTH_SHORT).show();

        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceTrasitionService.getErrorString(status.getStatusCode());
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    /*
    *
    *  ** Geofence **
    *
    * */


//    // create marker for Geofence creation
//    private void markerForGeofence(LatLng latLng){
//        Log.i(tAG, "markerForGeofence("+latLng+")");
//
//        String title = latLng.latitude + ", " + latLng.longitude;
//
//        // define marker option
//        MarkerOptions markerOptions = new MarkerOptions()
//                .position(latLng)
//                .title(title)
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
//
//        if (mMap != null){
//            // remove the last geoFenceMarker
//            if (geoFenceMarker != null){
//                geoFenceMarker.remove();
//            }
//
//            geoFenceMarker = mMap.addMarker(markerOptions);
//        }
//
//    }


//    public void ShowAllCities(View view) {
//
//        if (!client.isConnected()) {
//            Toast.makeText(this, "Map Not Connected", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            LocationServices.GeofencingApi.addGeofences(
//                    client,
//                    // The GeofenceRequest object.
//                     getGeofencingRequest(),
//                    // A pending intent that that is reused when calling removeGeofences(). This
//                    // pending intent is used to generate an intent when a matched geofence
//                    // transition is observed.
//                    createGeofencePendingIntent()
//            ).setResultCallback(this); // Result processed in onResult().
//        } catch (SecurityException se) {
//            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
//            Toast.makeText(this, se.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//
//    }

//    // create geoFence
//    private Geofence createGeofence(LatLng latLng, float radius){
//        Log.d(tAG, "createGeofence");
//
//        return new Geofence.Builder()
//                .setRequestId(GEOFENCE_REQ_ID)
//                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
//                .setExpirationDuration(GEO_DURATION)
//                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
//                        | Geofence.GEOFENCE_TRANSITION_EXIT)
//                .build();
//    }

    // create geoFence request
    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }


//    // Add the created GeofenceRequest to the device's monitoring list
//    private void addGeofence(GeofencingRequest request){
//        Log.d(tAG, "addGeofence");
//        if (checkPermission()){
//            LocationServices.GeofencingApi.addGeofences(
//                    client,
//                    request,
//                    createGeofencePendingIntent()
//            ).setResultCallback(this);
//        }
//    }


//    // draw geofence circle on google map
//    private void drawGeofence(){
//        Log.d(tAG, "drawGeofence()");
//
//        if (geoFenceLimits != null){
//            geoFenceLimits.remove();
//        }
//
//        CircleOptions circleOptions = new CircleOptions()
//                .center(geoFenceMarker.getPosition())
//                .strokeColor(Color.argb(50, 70,70,70))
//                .fillColor(Color.argb(100, 150,150,150))
//                .radius(GEOFENCE_RADIUS);
//
//        geoFenceLimits = mMap.addCircle(circleOptions);
//    }



//    // here we press on Geofence button
//    public void startGeofence(View view) {
//        Log.i(tAG, "startGeofence()");
//
//        if (geoFenceMarker != null){
//            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
//            GeofencingRequest geofencingRequest = createGeoFenceRequest(geofence);
//            addGeofence(geofencingRequest);
//        }else {
//            Log.e(tAG, "Geofence marker is null");
//        }
//    }

    /*
     *
     *  Notifications
     *
     * */

    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MapsActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }

    // we use PendingIntent object to call Intent service
    private PendingIntent createGeofencePendingIntent(){
        Log.d(tAG, "createGeofencePendingIntent");

        if (geoFencePendingIntent != null){
            return geoFencePendingIntent;
        }

        Intent intent = new Intent(MapsActivity.this, GeofenceTrasitionService.class);

        return PendingIntent.getService( // We use FLAG_UPDATE_CURRENT so that we get the same
                // pending intent back when calling addGeofences() and removeGeofences().
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    /*
     *
     *  Permissions
     *
     * */

    // check the permission to access to location
    private boolean checkPermission(){
        Log.d(tAG, "checkPermission");
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED );
    }

    // Ask for permission
    private void askPermission(){
        Log.d(tAG, "askPermission()");

        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQ_PERMISSION
        );
    }


    // Verify user's response of the permission requested
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.d(tAG, "onRequestPermissionsResult()");

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch ( requestCode ) {
            case REQ_PERMISSION: {
                if ( grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED ){
                    // Permission granted
                    getLastKnownLocation();

                } else {
                    // Permission denied
                    permissionsDenied();
                }
                break;
            }
        }
    }

    // App cannot work without the permissions
    private void permissionsDenied() {
        Log.w(tAG, "permissionsDenied()");
    }

}