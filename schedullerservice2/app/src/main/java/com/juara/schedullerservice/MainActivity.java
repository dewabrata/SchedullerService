package com.juara.schedullerservice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status> {
    private GoogleMap mMap;
    String TAG = "GPS";
    /**
     * Update interval of location request
     */
    private final int UPDATE_INTERVAL = 1000;

    /**
     * fastest possible interval of location request
     */
    private final int FASTEST_INTERVAL = 900;

    /**
     * LocationRequest instance
     */
    private LocationRequest locationRequest;

    /**
     * GoogleApiClient instance
     */
    private GoogleApiClient googleApiClient;

    /**
     * Location instance
     */
    private Location lastLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions(MainActivity.this);
      StartBackgroundTask();
        createGoogleApi();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);
    }

    private JobScheduler jobScheduler;
    private ComponentName componentName;
    private JobInfo jobInfo;

    public void StartBackgroundTask() {
        jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        componentName = new ComponentName(getApplicationContext(), MyService.class);
        jobInfo = new JobInfo.Builder(1, componentName)
                .setMinimumLatency(10000) //10 sec interval
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setRequiresCharging(false).build();
        jobScheduler.schedule(jobInfo);
    }

    public  boolean checkAndRequestPermissions(Context context) {

        List<String> listPermissionsNeeded = new ArrayList();
        int locationFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        int locationAccess = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION);
        //    int writefilePermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE);
        //    int cameraPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA);
        if (locationFine != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (locationAccess != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        //    if (writefilePermission != PackageManager.PERMISSION_GRANTED) {
        //        listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
        //    }
        //     if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
        //         listPermissionsNeeded.add(Manifest.permission.CAMERA);
        //     }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity)context, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 6969);
            return false;
        }
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMinZoomPreference(15);
        mMap.setMaxZoomPreference(20);





    }
    /**
     * Method is called when location is changed
     * @param location - location from fused location provider
     */
    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
        writeActualLocation(location);
    }

    /**
     * extract last location if location is not available
     */
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation() {
        //Log.d(TAG, "getLastKnownLocation()");
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (lastLocation != null) {
            Log.i(TAG, "LasKnown location. " +
                    "Long: " + lastLocation.getLongitude() +
                    " | Lat: " + lastLocation.getLatitude());
            writeLastLocation();
            startLocationUpdates();

        } else {
            Log.w(TAG, "No location retrieved yet");
            startLocationUpdates();

            //here we can show Alert to start location
        }
    }

    /**
     * this method writes location to text view or shared preferences
     * @param location - location from fused location provider
     */
    @SuppressLint("SetTextI18n")
    private void writeActualLocation(Location location) {

        if(mMap !=null) {
            LatLng cawang = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(cawang).title("Cawang"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(cawang));
        }
    }

    /**
     * this method only provokes writeActualLocation().
     */
    private void writeLastLocation() {
        writeActualLocation(lastLocation);
    }


    /**
     * this method fetches location from fused location provider and passes to writeLastLocation
     */
    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        //Log.i(TAG, "startLocationUpdates()");
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }





    /**
     * Create google api instance
     */
    private void createGoogleApi() {
        //Log.d(TAG, "createGoogleApi()");
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //connect google api
        googleApiClient.connect();

    }





    /**
     * this method tells whether google api client connected.
     * @param bundle - to get api instance
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Log.i(TAG, "onConnected()");
        getLastKnownLocation();
    }

    /**
     * this method returns whether connection is suspended
     * @param i - 0/1
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG,"connection suspended");
    }

    /**
     * this method checks connection status
     * @param connectionResult - connected or failed
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"connection failed");
    }

    /**
     * this method tells the result of status of google api client
     * @param status - success or failure
     */
    @Override
    public void onResult(@NonNull Status status) {
        Log.d(TAG,"result of google api client : " + status);
    }
}
