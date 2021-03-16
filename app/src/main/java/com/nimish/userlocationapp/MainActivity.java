package com.nimish.userlocationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private final CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
    Button getLocationBtn;
    TextView locationText;
    TextView longitudeText;
    TextView latitudeText;
    ProgressBar progressBar;
    final int LOCATION_PERMISSION_CODE = 44;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getLocationBtn = findViewById(R.id.location_btn);
        locationText = findViewById(R.id.display_location);
        longitudeText = findViewById(R.id.display_longitude);
        latitudeText = findViewById(R.id.display_latitude);
        progressBar = findViewById(R.id.progress_bar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        getLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    checkLocationService();
                    Task<Location> currentLocationTask = fusedLocationClient.getCurrentLocation(PRIORITY_HIGH_ACCURACY, cancellationTokenSource.getToken());
                    locationText.setText("");
                    latitudeText.setText("");
                    longitudeText.setText("");
                    progressBar.setVisibility(View.VISIBLE);
                    currentLocationTask.addOnCompleteListener(new OnCompleteListener<Location>() {
                        @Override
                        public void onComplete(@NonNull Task<Location> task) {
                            if(task.isSuccessful()){

                                Location location = task.getResult();
                                progressBar.setVisibility(View.INVISIBLE);

                                try {
                                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                                    String address = addresses.get(0).getAddressLine(0);
                                    String latitude = ""+addresses.get(0).getLatitude();
                                    String longitude = ""+addresses.get(0).getLongitude();
                                    locationText.setText(address);
                                    latitudeText.setText(latitude);
                                    longitudeText.setText(longitude);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                    });

                }else{
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_CODE);
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == LOCATION_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                checkLocationService();
            }else{
                Toast.makeText(getApplicationContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void checkLocationService(){
        final LocationManager manager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if(!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            dialogToEnableGPS();
        }
    }

    public void dialogToEnableGPS(){
        new AlertDialog.Builder(this)
                .setTitle("Enable Location")
                .setMessage("GPS Location is required to use the app")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).create().show();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancellationTokenSource.cancel();
    }
}