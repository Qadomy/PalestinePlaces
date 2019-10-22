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
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
//    private Button showAllLocationButton;

    // ****
    private static final String tag = MapsActivity2.class.getSimpleName();
    private final int UPDATE_INTERVAL =  5000;
    private final int FASTEST_INTERVAL = 3000;
    private final float SMALLEST_DISPLACEMENT = 10f; // 3 minutes
    private static final float GEOFIRE_RADIUS = 500.0f;
    private static String NOTIFICATION_CHANEL_ID = "multiple_locations";
    private static String NOTIFICATION_CHANEL_NAME = "My Notification";
    private static Float ZOOM_LOCATION_MAP = 14.0f;
    private static String NOTIFICATION_TITLE = "Palestine Cities";


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


                        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.map);
                        mapFragment.getMapAsync(MapsActivity2.this);

                        initArea();

                        settingGeoFire();


//                        showAllLocationButton = findViewById(R.id.showAllLocationButton);

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

    //
    private void buildLocationCallback() {
        Log.d(tag, "buildLocationCallback");

        locationCallback =  new LocationCallback(){
             @Override
             public void onLocationResult(final LocationResult locationResult) {
                 if (mMap != null){
                     if (currentLocation != null){
                         currentLocation.remove();
                     }

//                     Bitmap bit = BitmapFactory.decodeFile(String.valueOf(R.drawable.ic_current_locatio_car));
                     currentLocation = mMap.addMarker(new MarkerOptions()
                             .position(new LatLng(locationResult.getLastLocation().getLatitude(),
                                     locationResult.getLastLocation().getLongitude()))
                             .title("Current Location")
                             .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                     );

                     mMap.animateCamera(CameraUpdateFactory
                     .newLatLngZoom(currentLocation.getPosition(), ZOOM_LOCATION_MAP));

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
    private void  initArea(){

        locationsArea = new ArrayList<Location>();
        Location location6 = new Location(new LatLng(32.305972, 35.117517), "Anabta");
        Location location3 = new Location(new LatLng(32.319737, 35.054051), "Nour Shams");
        Location location7 = new Location(new LatLng(32.152914, 35.259108), "Huwwara");
        Location location4 = new Location(new LatLng(31.905446, 35.211472), "Blue Company");
        Location location2 = new Location(new LatLng(31.950327, 35.214190), "Jalazone");
        Location location11 = new Location(new LatLng(31.922862, 35.219852), "Al Huda Gas Station");
        Location location5 = new Location(new LatLng(31.919699, 35.207179), "Alarsal street");

        Location location1 = new Location(new LatLng(32.288742, 35.038921), "Home");
        Location location8 = new Location(new LatLng(32.121407, 35.257147), "Zaatarah");
        Location location10 = new Location(new LatLng(32.032800, 35.271455), "sanajul");
        Location location12 = new Location(new LatLng(19.629094, -155.459132), "Welcome");


        locationsArea.add(location1);
        locationsArea.add(location2);
        locationsArea.add(location3);
        locationsArea.add(location4);
        locationsArea.add(location5);
        locationsArea.add(location6);
        locationsArea.add(location7);
        locationsArea.add(location8);
        locationsArea.add(location10);
        locationsArea.add(location12);

        locationsArea.add(location11);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(tag, "onMapReady");

        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                    locationCallback, Looper.myLooper());
        }

        for (final Location location: locationsArea){

            // Add circles for locations
            mMap.addCircle(new CircleOptions()
                    .center(location.getLocationPoistion())
                    .radius(GEOFIRE_RADIUS)
                    .strokeColor(Color.argb(50, 70, 70, 70))
                    .fillColor(Color.argb(100, 150, 150, 150))
                    .strokeWidth(5.0f)
            );

            // set markers for locations
            mMap.addMarker(new MarkerOptions()
                    .position(location.getLocationPoistion())
                    .title(location.getLocationName())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                    .snippet("Click here to more info.")

            );

            mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                @Override
                public void onInfoWindowClick(Marker marker) {
                    Log.d(tag, "onInfoWindowClick: " + marker.getTitle() );

                    openAlertDialog(marker.getTitle());
                }
            });

            try {
                // create a GeoQuery when user reach a location in dangerousArea
                final GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(location.getLocationPoistion().latitude,
                        location.getLocationPoistion().longitude), GEOFIRE_RADIUS);

                final String locationName = location.getLocationName();

                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onKeyEntered(String key, GeoLocation location) {
                        Log.d(tag, "onKeyEntered");



                        Log.d(tag, "locationNameOnKeyEntered --> "+locationName);


                        //sendNotification(NOTIFICATION_TITLE, String.format("%s are entered "+locationName, key));

                        if (player!=null && player.isPlaying()){
                            player.stop();
                            playMusic(locationName);

                        }else {
                            playMusic(locationName);

                        }
                    }

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onKeyExited(String key) {
                        Log.d(tag, "onKeyExited");

                        //sendNotification(NOTIFICATION_TITLE, String.format("%s are leaving "+locationName, key));

                    }

                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public void onKeyMoved(String key, GeoLocation location) {
                        Log.d(tag, "onKeyMoved");

                       // sendNotification("Qadomy", String.format("%s are moving within  "+locationName, key));

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

            }catch (Exception ex){
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }


//    // here when click on Show All Locations Button in screen
//    public void showAllLocations(View view) {
//        Log.d(tag, "showAllLocations");
//
//
//        // here to hide the button after clicked
//        showAllLocationButton.setVisibility(View.INVISIBLE);
//
//    }// end of ShowAllLocation

    // here when click on any marker on map to open alert dialog
    private void openAlertDialog(final String locationName) {
        Log.d(tag, "openAlertDialog");

        Log.d(tag, "locationName = "+locationName);

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
                if (player != null && player.isPlaying()) {
                    player.stop();
                    playMusic(locationName);
                    playButton.setImageResource(R.drawable.ic_replay_button);

                } else  {
                    playMusic(locationName);
                    playButton.setImageResource(R.drawable.ic_replay_button);

                }


                //sendNotification(NOTIFICATION_TITLE, "You are listen to " + locationName + " sound");
            }
        });

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setNeutralButton("Stop Music",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                // here check if media player playing or no before stop it
                                if (player != null && player.isPlaying()) {
                                    player.stop();
                                }

                            }
                        })
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

            // config
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
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
        notification.sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        notificationManager.notify(1, notification);// here to make all notifications in one id

    }

    // play music
    private void playMusic(String locationName) {
        Log.d(tag, "playMusic");

        Log.d(tag, "the name arrive to play "+locationName);


        try {
            switch (locationName) {

                case "Welcome":
                    player = MediaPlayer.create(this,R.raw.welcome);
                    player.setLooping(false);
                    player.start();

                    break;
                case "Jalazone":
                    player = MediaPlayer.create(this, R.raw.jalazone);
                    player.setLooping(false);
                    player.start();

                    break;
                case "Nour Shams":
                    player = MediaPlayer.create(this, R.raw.noor);
                    player.setLooping(false);
                    player.start();

                    break;
                case "Blue Company":
                    player = MediaPlayer.create(this, R.raw.bluecompany);
                    player.setLooping(false);
                    player.start();

                    break;
                case "Alarsal street":
                    player = MediaPlayer.create(this, R.raw.alersaaal);
                    player.setLooping(false);
                    player.start();

                    break;
                case "Anabta":
                    player = MediaPlayer.create(this, R.raw.anabta);
                    player.setLooping(false);
                    player.start();

                    break;
                case "Huwwara":
                    player = MediaPlayer.create(this, R.raw.hewarah);
                    player.setLooping(false);
                    player.start();

                    break;

                case "Al Huda Gas Station":
                    player = MediaPlayer.create(this, R.raw.hoda);
                    player.setLooping(false);
                    player.start();

                    break;

                default:
                    player = MediaPlayer.create(this, R.raw.sorry);
                    player.setLooping(false);
                    player.start();

                    break;
            }
        }catch (Exception ex){
            Log.d(tag, "the exception here "+ex.getMessage());

        }

    }

}