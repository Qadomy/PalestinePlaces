package com.blue.palestineplaces;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.blue.palestineplaces.Classes.Location;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MapsActivity2 extends FragmentActivity implements
        OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocation;
    private DatabaseReference mLocationRef;
    private GeoFire geofire;
    private List<Location> locationsArea;
    private MediaPlayer player;

    // ****
    private static final String tag = MapsActivity2.class.getSimpleName();
    private final int UPDATE_INTERVAL =  5000; // 3 minutes
    private final int FASTEST_INTERVAL = 3000; // 3 minutes
    private final float SMALLEST_DISPLACEMENT = 10f; // 3 minutes
    private static final float GEOFENCE_RADIUS = 500.0f;
    private static String NOTIFICATION_CHANEL_ID = "12";
    private static String NOTIFICATION_CHANEL_NAME = "My Notification";


    /*
    *
    * onCreate
    *
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);

        Log.d(tag, "onCreate");


        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        buildLocationRequest();
                        buildLocationCallback();

                        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(
                                MapsActivity2.this);

                        initArea();

                        settingGeoFire();

                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity2.this);

                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        Toast.makeText(MapsActivity2.this, "You must enable permission", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission,
                                                                   PermissionToken token) {

                    }

                }).check();

    }// end of onCreate

    @Override
    protected void onStop() {
        Log.d(tag, "onStop");

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    // here for get the current location
    private void buildLocationRequest() {
        Log.d(tag, "buildLocationRequest");

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
    }


    private void buildLocationCallback() {
        Log.d(tag, "buildLocationCallback");

        locationCallback = new LocationCallback(){
             @Override
             public void onLocationResult(final LocationResult locationResult) {
                 if (mMap != null){
                     if (currentLocation != null){
                         currentLocation.remove();
                     }
                     currentLocation = mMap.addMarker(new MarkerOptions()
                             .position(new LatLng(locationResult.getLastLocation().getLatitude(),
                                     locationResult.getLastLocation().getLongitude()))
                             .title("Current Location")
                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                     );

                     mMap.animateCamera(CameraUpdateFactory
                     .newLatLngZoom(currentLocation.getPosition(), 14.0f));

                     geofire.setLocation("You",
                             new GeoLocation(locationResult.getLastLocation().getLatitude(),
                                     locationResult.getLastLocation().getLongitude()),
                             new GeoFire.CompletionListener() {
                                 @Override
                                 public void onComplete(String key, DatabaseError error) {

                                     if (currentLocation != null){
                                         currentLocation.remove();
                                     }
                                     currentLocation = mMap.addMarker(new MarkerOptions()
                                             .position(new LatLng(locationResult.getLastLocation().getLatitude(),
                                                     locationResult.getLastLocation().getLongitude()))
                                             .title("Current Location")
                                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                     );
                                 }
                             }
                     );
                 }
             }
         };
    }


    // setting the GeoFire
    private void settingGeoFire() {
        Log.d(tag,"settingGeoFire");

        mLocationRef = FirebaseDatabase.getInstance().getReference("MyLocation");
        geofire = new GeoFire(mLocationRef);
    }


    // create a list of locations in map
    private void initArea(){

        locationsArea = new ArrayList<Location>();

        Location location1 = new Location(new LatLng(32.288742, 35.038921), "Home");
        Location location2 = new Location(new LatLng(32.320124, 35.042970), "Hassouneh Gas Station");
        Location location3 = new Location(new LatLng(32.319737, 35.054051), "Nour Shams");
        Location location4 = new Location(new LatLng(31.905446, 35.211472), "Blue Company");
        Location location5 = new Location(new LatLng(31.914414, 35.207397), "Ramallah");
        Location location6 = new Location(new LatLng(32.217673, 35.177242), "Kedumim");
        Location location7 = new Location(new LatLng(32.152914, 35.259108), "Huwwara");
        Location location8 = new Location(new LatLng(32.121407, 35.257147), "Zaatarah");
//        Location location9 = new Location(new LatLng(31.913266, 35.215829), "شارع البيرة");
        Location location10 = new Location(new LatLng(32.032800, 35.271455), "sanajul");
        Location location11 = new Location(new LatLng(31.922862, 35.219852), "Al Huda Gas Station");


        locationsArea.add(location1);
        locationsArea.add(location2);
        locationsArea.add(location3);
        locationsArea.add(location4);
        locationsArea.add(location5);
        locationsArea.add(location6);
        locationsArea.add(location7);
        locationsArea.add(location8);
//        locationsArea.add(location9);
        locationsArea.add(location10);
        locationsArea.add(location11);



        FirebaseDatabase.getInstance().getReference("locations")
                .push()
                .setValue(locationsArea)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MapsActivity2.this, "locations updated successfully", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MapsActivity2.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(tag, "onMapReady");

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (fusedLocationProviderClient != null){
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
        }


        for (final Location location: locationsArea){
            mMap.addCircle(new CircleOptions()
                    .center(location.getLocationPoistion())
                    .radius(GEOFENCE_RADIUS)
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(100, 150, 150, 150))
                    .strokeWidth(5.0f)
            );

            mMap.addMarker(new MarkerOptions()
                    .position(location.getLocationPoistion())
                    .title(location.getLocationName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            );

            // here when clcik on marker to display the name of marker in alert dialog
            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    openAlertDialog(location.getLocationName());
                    return false;
                }
            });



            // create a GeoQuery when user reach a location in dangerousArea
            GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(location.getLocationPoistion().latitude,
                    location.getLocationPoistion().longitude), GEOFENCE_RADIUS);

            final String locationName = location.getLocationName();
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    Log.d(tag, "onKeyEntered");

                    sendNotification("Palestine Cities", String.format("%s are entered "+locationName, key));
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onKeyExited(String key) {
                    Log.d(tag, "onKeyExited");

                    sendNotification(location.getLocationName(), String.format("%s are leaving "+locationName, key));

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d(tag, "onKeyMoved");

                    //sendNotification("Qadomy", String.format("%s are moving within the "+locationName, key));

                }

                @Override
                public void onGeoQueryReady() {
                    Log.d(tag, "onGeoQueryReady");

                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                    Log.d(tag, "onGeoQueryError");

                    Toast.makeText(MapsActivity2.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }

            });
        }
    }

    // here when click on any marker on map to open alert dialog
    private void openAlertDialog(final String locationName) {

        LayoutInflater li = LayoutInflater.from(MapsActivity2.this);
        View promptsView = li.inflate(R.layout.layout_location_info, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MapsActivity2.this);

        alertDialogBuilder.setView(promptsView);
        final TextView titleName = promptsView.findViewById(R.id.locationName);
        final ImageView playButton = promptsView.findViewById(R.id.playSoundButton);

        titleName.setText(locationName);
        playButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                // when click on play image in the alert the music will stop and send notification again
                player.stop();
                sendNotification("Palestine Cities", "You listen to " + locationName + " again");
            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }


    // method for send notification to user
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void sendNotification(String title, String content) {
        Log.d(tag, "sendNotification");


        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANEL_ID,
                    NOTIFICATION_CHANEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();

            // config
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);
            notificationChannel.getLockscreenVisibility();
            notificationChannel.getLockscreenVisibility();

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANEL_ID);

        // here intent when click on the notification, it must resend to app
        Intent intent = new Intent(this, MapsActivity2.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(MapsActivity2.this, 0, intent, 0);

        builder.setContentText(content)
                .setAutoCancel(false)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_location))
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);


        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);

        playMusic();
    }

    // play music
    private void playMusic() {
        player = MediaPlayer.create(this, R.raw.ramallah);

        player.setLooping(false);
        player.start();
    }
}
