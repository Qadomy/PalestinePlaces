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
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

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
import com.mancj.materialsearchbar.MaterialSearchBar;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, ResultCallback<Status> {


    private static final String tAG = MapsActivity.class.getSimpleName();

    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private GoogleMap mMap;

    private Location lastlocation;

    private MaterialSearchBar searchBar;
    private GeofencingClient geofencingClient;

    private Marker locationMarker, geoFenceMarker;

    private Circle geoFenceLimits;

    private ArrayList<Geofence> geofenceList;

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


        createGoogleApi();
        // init the geofencingClient
        geofencingClient = LocationServices.getGeofencingClient (this);


    }// end of onCreate ..


    // Create a Intent send by the notification
    public static Intent makeNotificationIntent(Context context, String msg) {
        Intent intent = new Intent( context, MapsActivity.class );
        intent.putExtra( NOTIFICATION_MSG, msg );
        return intent;
    }


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


    // Call GoogleApiClient connection when starting the Activity
    @Override
    protected void onStart() {
        super.onStart();
        client.connect();
    }


    // Disconnect GoogleApiClient when stopping Activity
    @Override
    protected void onStop() {
        super.onStop();
        client.disconnect();
    }


    Circle circle;
    Marker marker;
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(tAG, "onMapReady()");

        mMap = googleMap;

        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerClickListener(this);


//        LatLng pos1 = new LatLng(31.905020, 35.211831);
//
//        // todo: ask for location permission
//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            return;
//
//        } else {
//            Log.d("weareher", "onMapReady");
//
//            client = new GoogleApiClient.Builder(this)
//                    .addApi(LocationServices.API)
//                    .addConnectionCallbacks(this)
//                    .addOnConnectionFailedListener(this)
//                    .build();
//
//            // connect the client
//            client.connect(); // so now after connect go to onConnect method
//            mMap.setMyLocationEnabled(true);
//
//
//
//
//
//            // set markers
//            marker = mMap.addMarker(new MarkerOptions()
//                        .position(pos1)
//                        .title("Position 1")
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
//
//            circle = mMap.addCircle(
//                        new CircleOptions()
//                            .center(pos1)
//                            .radius(500.0)
//                            .strokeWidth(3f)
//                            .strokeColor(Color.RED)
//                            .fillColor(Color.argb(70, 150, 50, 50))
//            );
//
//
//        }


    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(tAG, "onLocationChanged ["+location+"]");

        lastlocation = location;
        writeActualLocation(location);

//        // get the current locations longitude and latitude
//        if (location == null) {
//
//            Toast.makeText(this, "Location not found", Toast.LENGTH_LONG).show();
//
//        } else {
//
//            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
//            mMap.animateCamera(update);
//            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//            MarkerOptions options = new MarkerOptions();
//            options.position(latLng);
//            options.title("Current Locations");
//            mMap.addMarker(options);
//
//
//            float[] distance = new float[2];
//
//
//            try {
//                Location.distanceBetween(
//                        location.getLatitude(),
//                        location.getLongitude(),
//                        circle.getCenter().latitude,
//                        circle.getCenter().longitude,
//                        distance
//                );
//
//
//                if (distance[0] > circle.getRadius()) {
//
//                    Log.d("exceptiondistanc", "Outside");
//
//                } else {
//
//                    String markerTitle;
//                    markerTitle = marker.getTitle();
//
//                    Log.d("exceptiondistanc", "I´ in the circle" + " " + markerTitle);
//
//                }
//
//            }catch (Exception e){
//                Log.d("exceptiondistanc", e.getMessage());
//            }
//
//
//        }


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


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(tAG, "onConnected()");

        getLastKnownLocation();

//        // after client connected
//        request = new LocationRequest().create();
//        //request.setInterval(1000); // time for request
//        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//        if (ActivityCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this,
//                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//
//            return;
//        } else {
//            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
//            Log.d("weareher", "FusedLocationApi done");
//        }

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

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(tAG, "onConnectionSuspended()");


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.w(tAG, "onConnectionFailed()");

    }

    // called when click on any part of map
    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(tAG, "onMapClick("+latLng +")");

        markerForGeofence(latLng);

    }

    // create marker for Geofence creation
    private void markerForGeofence(LatLng latLng){
        Log.i(tAG, "markerForGeofence("+latLng+")");

        String title = latLng.latitude + ", " + latLng.longitude;

        // define marker option
        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)
                .title(title)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));

        if (mMap != null){
            // remove the last geoFenceMarker
            if (geoFenceMarker != null){
                geoFenceMarker.remove();
            }

            geoFenceMarker = mMap.addMarker(markerOptions);
        }



    }

    // called when Marker is touched
    @Override
    public boolean onMarkerClick(Marker marker) {
        Log.d(tAG, "onMarkerClickListener: " + marker.getPosition() );
        return false;
    }


    /*
    *
    *
    *
    * */

    // create geoFence
    private Geofence createGeofence(LatLng latLng, float radius){
        Log.d(tAG, "createGeofence");

        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    // create geoFence request
    private GeofencingRequest createGeoFenceRequest(Geofence geofence){
        Log.d(tAG, "createGeofenceRequest");

        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }


    // Add the created GeofenceRequest to the device's monitoring list
    private void addGeofence(GeofencingRequest request){
        Log.d(tAG, "addGeofence");
        if (checkPermission()){
            LocationServices.GeofencingApi.addGeofences(
                    client,
                    request,
                    createGeofencePendingIntent()
            ).setResultCallback(this);
        }
    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.i(tAG, "onResult: " + status);
        if (status.isSuccess()){
            drawGeofence();
        }else{
            // inform about fail
        }
    }

    // draw geofence circle on google map
    private void drawGeofence(){
        Log.d(tAG, "drawGeofence()");

        if (geoFenceLimits != null){
            geoFenceLimits.remove();
        }

        CircleOptions circleOptions = new CircleOptions()
                .center(geoFenceMarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor(Color.argb(100, 150,150,150))
                .radius(GEOFENCE_RADIUS);

        geoFenceLimits = mMap.addCircle(circleOptions);
    }


    // for geofence menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
            case R.id.geofence_id: {
                startGeofence();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    // Start Geofence creation process
    private void startGeofence(){
        Log.i(tAG, "startGeofence()");

        if (geoFenceMarker != null){
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofencingRequest = createGeoFenceRequest(geofence);
            addGeofence(geofencingRequest);
        }else {
            Log.e(tAG, "Geofence marker is null");
        }
    }


    public void startGeofence(View view) {
        Log.i(tAG, "startGeofence()");

        if (geoFenceMarker != null){
            Geofence geofence = createGeofence(geoFenceMarker.getPosition(), GEOFENCE_RADIUS);
            GeofencingRequest geofencingRequest = createGeoFenceRequest(geofence);
            addGeofence(geofencingRequest);
        }else {
            Log.e(tAG, "Geofence marker is null");
        }
    }

    /*
     *
     *
     *
     * */

    // we use PendingIntent object to call Intent service
    private PendingIntent createGeofencePendingIntent(){
        Log.d(tAG, "createGeofencePendingIntent");

        if (geoFencePendingIntent != null){
            return geoFencePendingIntent;
        }

        Intent intent = new Intent(MapsActivity.this, GeofenceTrasitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT
        );
    }

    /*
     *
     *
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
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
