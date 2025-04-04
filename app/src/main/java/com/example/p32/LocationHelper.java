package com.example.p32;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public class LocationHelper {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private final Context context;
    private final LocationManager locationManager;
    private LocationCallback callback;

    // Interface for location callback
    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onLocationError(String errorMessage);
    }

    public LocationHelper(Context context) {
        this.context = context;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        if (context instanceof Activity) {
            ((Activity) context).startActivity(intent);
        } else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
            // Request a single location update
    @SuppressLint("MissingPermission")
    public void requestSingleLocationUpdate(LocationCallback callback) {
        this.callback = callback;

        // Check location permissions
        if (!checkLocationPermissions()) {
            callback.onLocationError("Location permissions not granted");
            requestPermissions();
//            return;
        }

        // Check if GPS or Network providers are enabled
        if (!isLocationProvidersEnabled()) {
            callback.onLocationError("Enable location service to proceed");
            openLocationSettings();
//            return;
        }

        // Create a one-time location listener
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                // Pass the location to the callback
                new Handler(Looper.getMainLooper()).post(() -> callback.onLocationReceived(
                        location.getLatitude(),
                        location.getLongitude()
                ));
                // Stop further updates
                locationManager.removeUpdates(this);
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                new Handler(Looper.getMainLooper()).post(() -> callback.onLocationError("Enable location service to proceed "));
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                // No-op
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                // No-op
            }
        };

        // Request location updates (using both GPS and Network providers)
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, 0, locationListener, Looper.getMainLooper()
            );
        } catch (Exception e) {
            callback.onLocationError(" Enable location service to proceed " + e.getMessage());
        }
    }

    // Check if location permissions are granted
    private boolean checkLocationPermissions() {
        return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // Request location permissions
    private void requestPermissions() {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions(
                    (Activity) context,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        } else if (callback != null) {
            callback.onLocationError("Cannot request permissions: context is not an Activity");
        }
    }

    // Check if location providers are enabled
    private boolean isLocationProvidersEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
}
