package com.example.locationtracker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    @Nullable
    private Location mLatestLocation;
    private ArrayList<Location> mPathLocations = new ArrayList<>();

    private boolean mIsLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this.getApplicationContext());

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();

                //TODO: Check for location distance
                mLatestLocation = location;
                mPathLocations.add(location);
                renderMap();
            }
        };

        initMap();

        initLocationRequest();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissions();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeLocationUpdates();
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        getLastLocation();
    }

    private void initLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000L);
        mLocationRequest.setFastestInterval(5000L);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void checkPermissions() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        requestLocationUpdates();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();
    }

    @SuppressLint("MissingPermission")
    private void requestLocationUpdates() {
        mFusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper()
        );
    }

    private void removeLocationUpdates() {
        mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLatestLocation = task.getResult();
                            renderMap();
                        } else {
                            //TODO: Show error message
                        }
                    }
                });
    }

    private void renderMap() {
        if (mLatestLocation != null) {
            mMap.clear();

            LatLng latLng = new LatLng(
                    mLatestLocation.getLatitude(),
                    mLatestLocation.getLongitude()
            );

            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title("Current location")
            );

            renderPath(mPathLocations);

            if (!mIsLoaded) {
                mIsLoaded = true;
                mMap.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(latLng, 15f));
            }
        }
    }

    private void renderPath(List<Location> path) {
        if (mMap == null || path.size() < 2) return;

        PolylineOptions options = new PolylineOptions();
        options.color(Color.RED);
        options.width(6f);

        for (Location location : path) {
            LatLng latLng = new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
            );
            options.add(latLng);
        }

        mMap.addPolyline(options);
    }
}