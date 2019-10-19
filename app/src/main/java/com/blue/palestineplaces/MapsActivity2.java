package com.blue.palestineplaces;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
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
    private MediaPlayer mMediaPlayer;

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

    // here for get the current loaction
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
        Location location2 = new Location(new LatLng(32.320124, 35.042970), "كازية حسونة");
        Location location3 = new Location(new LatLng(32.319737, 35.054051), "نور شمس");
        Location location4 = new Location(new LatLng(31.905446, 35.211472), "Blue Company");
        Location location5 = new Location(new LatLng(31.904948, 35.204439), "دوار المنارة");


        locationsArea.add(location1);
        locationsArea.add(location2);
        locationsArea.add(location3);
        locationsArea.add(location4);
        locationsArea.add(location5);

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


        for (Location location: locationsArea){
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

            // create a GeoQuery when user reach a location in dangerousArea
            GeoQuery geoQuery = geofire.queryAtLocation(new GeoLocation(location.getLocationPoistion().latitude,
                    location.getLocationPoistion().longitude), GEOFENCE_RADIUS);

            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    Log.d(tag, "onKeyEntered");

                    sendNotification("Qadomy", String.format("%s entered the location", key));
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onKeyExited(String key) {
                    Log.d(tag, "onKeyExited");

                    sendNotification("Qadomy", String.format("%s leaving the location", key));

                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    Log.d(tag, "onKeyMoved");

                    //sendNotification("Qadomy", String.format("%s move within the location", key));

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


    /*
    *
    * methods implements from GeoQueryEventListener
    *
    * */




    /*
     *
     * End of methods implements from GeoQueryEventListener
     *
     * */


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

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                NOTIFICATION_CHANEL_ID);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/"));
        PendingIntent pendingIntent = PendingIntent.getActivity(MapsActivity2.this, 0, intent, 0);

        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(false)
                .setContentTitle("Notification title")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_location));

        try {
            Uri notification = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.ramallah);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();

        } catch (Exception e) {
            e.printStackTrace();
        }

        Notification notification = builder.build();
        notificationManager.notify(new Random().nextInt(), notification);


    }

}
