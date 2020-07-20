package com.example.lookscreen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import com.example.lookscreen.Service.MyService;

import static android.content.Context.VIBRATOR_SERVICE;

public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "MyReceiver";
    private boolean screenOff;
    Vibrator vibrator;
    Context context;

    public MyReceiver() {
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            Log.d(TAG, "onReceive: "+Intent.ACTION_SCREEN_OFF);
            screenOff = true;

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            Log.d(TAG, "onReceive: "+Intent.ACTION_SCREEN_ON);
            screenOff = false;
        }

        Intent i = new Intent(context, MyService.class);
        i.putExtra("screen_state", screenOff);
        context.startService(i);
    }
}
