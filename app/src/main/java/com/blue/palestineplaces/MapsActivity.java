package com.blue.palestineplaces;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
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
import com.google.maps.android.kml.KmlLayer;
import com.mancj.materialsearchbar.MaterialSearchBar;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    GoogleApiClient client;
    LocationRequest request;
    private GoogleMap mMap;


    private MaterialSearchBar searchBar;
    private GeofencingClient geofencingClient;


    private ArrayList<Geofence> geofenceList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // init the geofencingClient
        geofencingClient = LocationServices.getGeofencingClient(this);

//        geofenceList.add(new Geofence.Builder()
//                .setRequestId()
//
//                .build()
//        )


    }// end of onCreate ..


    Circle circle;
    Marker marker;
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        LatLng pos1 = new LatLng(31.905020, 35.211831);

        // todo: ask for location permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        } else {
            Log.d("weareher", "onMapReady");

            client = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            // connect the client
            client.connect(); // so now after connect go to onConnect method
            mMap.setMyLocationEnabled(true);


            mMap.setOnMapClickListener(this);
            //mMap.setOnMarkerClickListener(this);

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);


            // set markers
            marker = mMap.addMarker(new MarkerOptions()
                        .position(pos1)
                        .title("Position 1")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

            circle = mMap.addCircle(
                        new CircleOptions()
                            .center(pos1)
                            .radius(500.0)
                            .strokeWidth(3f)
                            .strokeColor(Color.RED)
                            .fillColor(Color.argb(70, 150, 50, 50))
            );


        }


    }

    @Override
    public void onLocationChanged(Location location) {



        // get the current locations longitude and latitude
        if (location == null) {

            Toast.makeText(this, "Location not found", Toast.LENGTH_LONG).show();

        } else {

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 15);
            mMap.animateCamera(update);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            MarkerOptions options = new MarkerOptions();
            options.position(latLng);
            options.title("Current Locations");
            mMap.addMarker(options);


            float[] distance = new float[2];


            try {
                Location.distanceBetween(
                        location.getLatitude(),
                        location.getLongitude(),
                        circle.getCenter().latitude,
                        circle.getCenter().longitude,
                        distance
                );


                if (distance[0] > circle.getRadius()) {

                    Log.d("exceptiondistanc", "Outside");

                } else {

                    String markerTitle;
                    markerTitle = marker.getTitle();

                    Log.d("exceptiondistanc", "I´ in the circle" + " " + markerTitle);

                }

            }catch (Exception e){
                Log.d("exceptiondistanc", e.getMessage());
            }


        }


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // after client connected
        request = new LocationRequest().create();
        //request.setInterval(1000); // time for request
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        Log.d("weareher", "onConnected");
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
            Log.d("weareher", "FusedLocationApi done");
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("weareher", "onConnectionSuspended");


    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("weareher", "onConnectionFailed");

    }

    @Override
    public void onMapClick(LatLng latLng) {
        // if user click on any part of map

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
