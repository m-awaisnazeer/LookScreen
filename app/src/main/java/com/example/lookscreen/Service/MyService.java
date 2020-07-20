package com.example.lookscreen.Service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.lookscreen.MyReceiver;

import java.security.Provider;

public class MyService extends Service {
    private static final String TAG = "MyService";
    BroadcastReceiver mReceiver;
    public static int countOn = 0;
    public static int countOff = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        // register receiver that handles screen on and screen off logic
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        mReceiver = new MyReceiver();
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(mReceiver);
        Log.d(TAG, "onDestroy: called");

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        boolean screenOn = intent.getBooleanExtra("screen_state", false);
        if (!screenOn) {
            Log.d(TAG, "onStartCommand: called");
            Log.d(TAG, "CountOn =" + countOn);

            Toast.makeText(getApplicationContext(), "Awake", Toast.LENGTH_LONG)
                    .show();
        } else {
            Log.d(TAG, "Called");
            Log.d(TAG, "CountOff =" + countOff);
        }


        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }
}
