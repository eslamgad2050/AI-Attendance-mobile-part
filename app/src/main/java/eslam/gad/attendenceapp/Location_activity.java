package eslam.gad.attendenceapp;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


public class Location_activity extends AppCompatActivity {
    protected static final int REQUEST_CHECK_SETTINGS = 9999;
    ProgressBar progressBar;
    long national_id = 0;

    Button attend_button, leave_button;
    private FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;

    Location location = null;
    boolean permission_granted = false, settings_ok = false;
    String TAG = "main_location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_location);

        check_settings();
        get_permission();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (permission_granted && settings_ok) {
                    get_location();
                } else {
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        leave_button = (Button) findViewById(R.id.leave);
        attend_button = (Button) findViewById(R.id.attend);

        national_id = getIntent().getLongExtra("national_id", 0);
        attend_button.setOnClickListener(attend);
        leave_button.setOnClickListener(leave);
    }

    void get_location() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location l) {
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Logic to handle location object
                    location = l;
                    progressBar.setVisibility(View.GONE);
                    leave_button.setVisibility(View.VISIBLE);
                    attend_button.setVisibility(View.VISIBLE);
                } else {

                    if (permission_granted)
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            settings_ok = true;
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    void check_settings() {
        locationRequest = (new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)).build();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                settings_ok = true;
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(Location_activity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    void get_permission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.
                checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.
                PERMISSION_GRANTED) {
            ActivityResultLauncher<String[]> locationPermissionRequest =
                    registerForActivityResult(new ActivityResultContracts
                                    .RequestMultiplePermissions(), result -> {
                                Boolean fineLocationGranted = result.getOrDefault(
                                        ACCESS_FINE_LOCATION, false);
                                Boolean coarseLocationGranted = result.getOrDefault(
                                        ACCESS_COARSE_LOCATION, false);
                                if (fineLocationGranted != null && fineLocationGranted) {
                                    // Precise location access granted.
                                    permission_granted = true;
                                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                    // Only approximate location access granted.
                                } else {
                                    // No location access granted.
                                }
                            }
                    );
            locationPermissionRequest.launch(new String[]{
                    ACCESS_FINE_LOCATION,
                    ACCESS_COARSE_LOCATION
            });
        } else {
            permission_granted = true;
        }

    }


    View.OnClickListener attend = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Location_activity.this, Camera_activity.class);
            intent.putExtra("in_ot_out", true);//true means attending
            intent.putExtra("national_id", national_id);
            intent.putExtra("location", new double[]{location.getLatitude(), location.getLongitude()});//1 means attending
            startActivity(intent);

        }
    };
    View.OnClickListener leave = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Location_activity.this, Camera_activity.class);
            intent.putExtra("in_or_out", false);//false means leaving
            intent.putExtra("national_id", national_id);
            intent.putExtra("location", new double[]{location.getLatitude(), location.getLongitude()});//1 means attending
            startActivity(intent);
        }
    };

    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location l : locationResult.getLocations()) {
                // Update UI with location data
                // ...
                if (l != null) {
                    location = l;
                    Log.d(TAG, "onLocationResult: location lat:" + location.getLatitude() + " long " + location.getLongitude());
                    fusedLocationClient.removeLocationUpdates(locationCallback);
                    progressBar.setVisibility(View.GONE);
                    leave_button.setVisibility(View.VISIBLE);
                    attend_button.setVisibility(View.VISIBLE);
                }
            }
        }
    };
}
