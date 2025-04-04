package com.example.p32;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

public class SMSHelper {

    private Context context;

    public SMSHelper(Context context) {
        this.context = context;
    }

    // Callback interface to notify when SMS is sent
    public interface SmsSendCallback {
        void onSmsSent(boolean success);
    }
    // Method to fetch all messages
    public List<String> getAllMessages() {
        List<String> messages = new ArrayList<>();
        Uri smsUri = Telephony.Sms.CONTENT_URI;  // Uri for SMS content provider
        ContentResolver contentResolver = context.getContentResolver();

        // Querying the SMS content provider
        Cursor cursor = contentResolver.query(smsUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int bodyColumnIndex = cursor.getColumnIndex(Telephony.Sms.BODY);  // Column index for SMS body
            int addressColumnIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS);  // Column index for sender address
            int dateColumnIndex = cursor.getColumnIndex(Telephony.Sms.DATE);  // Column index for date of the SMS

            do {
                String body = cursor.getString(bodyColumnIndex);
                String address = cursor.getString(addressColumnIndex);
                String date = cursor.getString(dateColumnIndex);

                // Add message details to the list
                messages.add("From: " + address + "\nDate: " + date + "\nMessage: " + body + "\n");

            } while (cursor.moveToNext());

            cursor.close();
        } else {
            Log.e("SMSHelper", "No SMS messages found or failed to query.");
        }

        return messages;
    }

    public static void sendSmsFromPhone(final String phoneNumber, final String message, final SmsSendCallback callback) {
        // Your SMS sending code here
        // Assuming you're using something like SmsManager to send the SMS
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            // Notify callback that SMS has been sent
            if (callback != null) {
                callback.onSmsSent(true);  // Pass true if the SMS is sent successfully
            }
        } catch (Exception e) {
            if (callback != null) {
                callback.onSmsSent(false);  // Pass false if an error occurs
            }
        }
    }

}
