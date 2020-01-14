package com.juara.schedullerservice;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.juara.schedullerservice.model.dataGpsTracking.DataGpsTracking;
import com.juara.schedullerservice.service.APIClient;
import com.juara.schedullerservice.service.APIInterfacesRest;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by root on 12/2/18.
 */
@SuppressLint("NewApi")
public class MyService extends JobService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        ResultCallback<Status> {

    /**
     * Update interval of location request
     */
    private final int UPDATE_INTERVAL = 10000;

    /**
     * fastest possible interval of location request
     */
    private final int FASTEST_INTERVAL = 9000;

    /**
     * The Job scheduler.
     */
    JobScheduler jobScheduler;

    /**
     * The Tag.
     */
    String TAG = "MyService";

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
        APIInterfacesRest apiInterface;
        ProgressDialog progressDialog;
        Log.d(TAG, location.getLatitude() + ", " + location.getLongitude());

        apiInterface = APIClient.getClient().create(APIInterfacesRest.class);
        progressDialog = new ProgressDialog(MyService.this);
        progressDialog.setTitle("Loading");
//        progressDialog.show();
        Call<DataGpsTracking> call3 = apiInterface.getDataGpsTracking(String.valueOf(location.getLatitude()),String.valueOf(location.getLongitude()),"sofii");
        call3.enqueue(new Callback<DataGpsTracking>() {
            @Override
            public void onResponse(Call<DataGpsTracking> call, Response<DataGpsTracking> response) {
               // progressDialog.dismiss();
                DataGpsTracking data = response.body();

                if (data !=null) {


                    Toast.makeText(MyService.this,data.getMessage(),Toast.LENGTH_LONG).show();





                }else{

                    try {
                        JSONObject jObjError = new JSONObject(response.errorBody().string());
                        Toast.makeText(MyService.this, jObjError.getString("message"), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(MyService.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

            }

            @Override
            public void onFailure(Call<DataGpsTracking> call, Throwable t) {
             //   progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"Maaf koneksi bermasalah",Toast.LENGTH_LONG).show();
                call.cancel();
            }
        });





        //here in this method you can use web service or any other thing
        Toast.makeText(getApplicationContext(),location.getLatitude() + ", " + location.getLongitude(),Toast.LENGTH_SHORT).show();
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
     * Default method of service
     * @param params - JobParameters params
     * @return boolean
     */
    @Override
    public boolean onStartJob(JobParameters params) {

        startJobAgain();

        createGoogleApi();

        return false;
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
     * disconnect google api
     * @param params - JobParameters params
     * @return result
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        googleApiClient.disconnect();
        return false;
    }

    /**
     * starting job again
     */
    private void startJobAgain() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d(TAG, "Job Started");
            ComponentName componentName = new ComponentName(getApplicationContext(),
                    MyService.class);
            jobScheduler = (JobScheduler) getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
            JobInfo jobInfo = new JobInfo.Builder(1, componentName)
                    .setMinimumLatency(10000) //10 sec interval
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setRequiresCharging(false).build();
            jobScheduler.schedule(jobInfo);
        }
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