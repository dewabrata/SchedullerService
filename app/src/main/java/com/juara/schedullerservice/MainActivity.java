package com.juara.schedullerservice;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
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
import com.juara.schedullerservice.model.dataGpsTracking.DataGpsTracking;
import com.juara.schedullerservice.model.dataLokasi.DataLokasiKita;
import com.juara.schedullerservice.model.dataLokasi.GpsTracking;
import com.juara.schedullerservice.service.APIClient;
import com.juara.schedullerservice.service.APIInterfacesRest;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private final int UPDATE_INTERVAL = 100000;

    /**
     * fastest possible interval of location request
     */
    private final int FASTEST_INTERVAL = 90000;

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

    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    public void ulang() {
     Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

            synchronized public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getData();
                    }
                });


            }

        }, TimeUnit.SECONDS.toMillis(1), TimeUnit.SECONDS.toMillis(20));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkAndRequestPermissions(MainActivity.this);
   //     StartBackgroundTask();
        createGoogleApi();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.g_map);
        mapFragment.getMapAsync(this);



      ulang();
    }

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
    public void onConnected(@Nullable Bundle bundle) {
        getLastKnownLocation();

    }
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

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG,"connection failed");

    }

    @Override
    public void onResult(@NonNull Status status) {
        Log.d(TAG,"result of google api client : " + status);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged [" + location + "]");
        lastLocation = location;
        writeActualLocation(location);

    }
    @SuppressLint("SetTextI18n")
    private void writeActualLocation(final Location location) {
        //do something here
    }

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMinZoomPreference(15);
        mMap.setMaxZoomPreference(20);

    }

    public void getData(){
        APIInterfacesRest apiInterface;
        ProgressDialog progressDialog;

        apiInterface = APIClient.getClient().create(APIInterfacesRest.class);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading");
//        progressDialog.show();
        Call<DataLokasiKita> call3 = apiInterface.getDataLokasi();
        call3.enqueue(new Callback<DataLokasiKita>() {
            @Override
            public void onResponse(Call<DataLokasiKita> call, Response<DataLokasiKita> response) {
                // progressDialog.dismiss();
                DataLokasiKita data = response.body();

                List<ModelGPSData> gpsdata = new ArrayList<ModelGPSData>();
                if (data !=null) {

                    List<GpsTracking> datax = data.getData().getGpsTracking() ;

                    String usernameDummy="";
                    String latitude = "";
                    String longitude ="";
                    for(int x = 0 ; x < datax.size();x++){

                        if (!datax.get(x).getUsername().equalsIgnoreCase(usernameDummy)){

                          if(x >0) {
                              ModelGPSData mgd = new ModelGPSData();

                              mgd.setNama(usernameDummy);
                              mgd.setLatitude(latitude);
                              mgd.setLongitude(longitude);

                              gpsdata.add(mgd);

                          }
                            usernameDummy = datax.get(x).getUsername();




                        }else{
                            latitude = datax.get(x).getLatitude();
                            longitude = datax.get(x).getLongitude();
                        }


                    }
                    ModelGPSData mgd = new ModelGPSData();

                    mgd.setNama(usernameDummy);
                    mgd.setLatitude(latitude);
                    mgd.setLongitude(longitude);

                    gpsdata.add(mgd);
                    Log.d("test","test");

                    if(mMap !=null) {
                        mMap.clear();
                        LatLng cawang = null;
                        for(int y = 0 ; y < gpsdata.size();y++) {

                            if (!gpsdata.get(y).getLatitude().equalsIgnoreCase("")) {
                                cawang = new LatLng(Double.parseDouble(gpsdata.get(y).getLatitude()), Double.parseDouble(gpsdata.get(y).getLongitude()));
                                mMap.addMarker(new MarkerOptions().position(cawang).title(gpsdata.get(y).getNama()));
                            }
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(cawang));
                    }



                }else{

                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(MainActivity.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<DataLokasiKita> call, Throwable t) {
                //   progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Maaf koneksi bermasalah",Toast.LENGTH_LONG).show();
                call.cancel();
            }
        });
    }


    class ModelGPSData{

        private String nama;

        public String getNama() {
            return nama;
        }

        public void setNama(String nama) {
            this.nama = nama;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        private String latitude;
        private String longitude;

    }
}
