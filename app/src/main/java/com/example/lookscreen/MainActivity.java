package com.example.lookscreen;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.lookscreen.Service.MyService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ON_DO_NOT_DISTURB_CALLBACK_CODE = 1;
    FloatingActionButton fab_lock;
    SharedPreferences appPreferences;
    boolean isFirstRun = false;
    public static final int RESULT_ENABLE = 11;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName compName;
    Vibrator vibrator;
    AudioManager audioManager;
    int previous_settings;
    NotificationManager notificationManager;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fab_lock = findViewById(R.id.fab_lock);

        devicePolicyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
        compName = new ComponentName(this, MyAdmin.class);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        isFirstRun = appPreferences.getBoolean("isFirstRun", false);

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Log.d(TAG, Intent.ACTION_SCREEN_OFF);
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(200);
                    }
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)&& intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    Log.d(TAG, Intent.ACTION_SCREEN_ON);
                    Toast.makeText(context, "Screen On", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= 26) {
                        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        vibrator.vibrate(200);
                    }
                    Toast.makeText(context, "Screen On", Toast.LENGTH_SHORT).show();
                }
            }
        };

        //Silent Mode
        requestMutePermissions();


        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(broadcastReceiver, intentFilter);
        startService(new Intent(MainActivity.this, MyService.class));
        fab_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lockScreen();

            }
        });
    }

    private void lockScreen() {
        boolean active = devicePolicyManager.isAdminActive(compName);

        if (active) {
            devicePolicyManager.lockNow();

        } else {
            Toast.makeText(this, "You need to enable the Admin Device Features", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why we need this permission");
            startActivityForResult(intent, RESULT_ENABLE);

        }
    }

    private void appshortcupdialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setCancelable(false);
        dialog.setTitle("App short cut");
        dialog.setMessage("Do you want to create app short cut");
        dialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N_MR1)
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                ShortcutManager shortcutManager =
                        getApplicationContext().getSystemService(ShortcutManager.class);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (shortcutManager.isRequestPinShortcutSupported()) {

                        ShortcutInfo pinShortcutInfo =
                                new ShortcutInfo.Builder(getApplicationContext(), "my-shortcut")
                                        .setShortLabel("Lock Screen")
                                        .setLongLabel("Lock Device")
                                        .setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.short_cut_icon))
                                        .setIntent(new Intent(Intent.ACTION_VIEW,null,MainActivity.this,LockScreen.class))
                                        .build();


                        Intent pinnedShortcutCallbackIntent =
                                shortcutManager.createShortcutResultIntent(pinShortcutInfo);


                        PendingIntent successCallback = PendingIntent.getBroadcast(getApplicationContext(), /* request code */ 0,
                                pinnedShortcutCallbackIntent, /* flags */ 0);

                        shortcutManager.requestPinShortcut(pinShortcutInfo,
                                successCallback.getIntentSender());
                        SharedPreferences.Editor editor = appPreferences.edit();
                        editor.putBoolean("isFirstRun", true);
                        editor.commit();


                    }
                }
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RESULT_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(MainActivity.this, "You have enabled the Admin Device features", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Problem to enable the Admin Device features", Toast.LENGTH_SHORT).show();
                }
                break;
        }

        if (requestCode == ON_DO_NOT_DISTURB_CALLBACK_CODE) {
            this.requestForDoNotDisturbPermissionOrSetDoNotDisturbForApi23AndUp();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void requestMutePermissions() {
        try {
            if (Build.VERSION.SDK_INT < 23) {
                audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                previous_settings = audioManager.getMode();
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

            } else if (Build.VERSION.SDK_INT >= 23) {
                this.requestForDoNotDisturbPermissionOrSetDoNotDisturbForApi23AndUp();
            }
        } catch (SecurityException e) {

        }
    }

    private void requestForDoNotDisturbPermissionOrSetDoNotDisturbForApi23AndUp() {

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        // if user granted access else ask for permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted()) {
                AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                previous_settings = notificationManager.getCurrentInterruptionFilter();
                audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            } else {
                // Open Setting screen to ask for permisssion
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivityForResult(intent, ON_DO_NOT_DISTURB_CALLBACK_CODE);
            }
        }
    }


//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (Build.VERSION.SDK_INT<23){
//            audioManager.setRingerMode(previous_settings);
//            Log.d(TAG, "onStop: ");
//        }else {
//            notificationManager.setInterruptionFilter(previous_settings);
//            Log.d(TAG, "onStop: ");
//        }
//    }
//
//    @Override
//    protected void onPause() {
//
//        super.onPause();
//        if (Build.VERSION.SDK_INT<23){
//            audioManager.setRingerMode(previous_settings);
//            Log.d(TAG, "onPause: ");
//        }else {
//            notificationManager.setInterruptionFilter(previous_settings);
//            Log.d(TAG, "onPause: ");
//        }
////
////
////    }
////        @Override
////    protected void onResume() {
////        super.onResume();
////        requestMutePermissions();
////        Log.d(TAG, "onResume: ");
////    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.setInterruptionFilter(previous_settings);
        }else {
            audioManager.setRingerMode(previous_settings);
        }
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                return true;
//            case R.id.enable_auto_silent_mode:
//                //requestMutePermissions();
//                return true;
            case R.id.create_short_cut:
                if (isFirstRun == false) {
                     appshortcupdialog();
                } else {
                    Toast.makeText(this, "Short Already Created", Toast.LENGTH_SHORT).show();

                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}