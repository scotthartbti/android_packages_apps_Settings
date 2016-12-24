package com.android.settings.beanstalk.utils;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.utils.CMDProcessor;

import java.io.IOException;
import java.util.List;

public class OnBoot extends BroadcastReceiver {

    Context settingsContext = null;
    private static final String TAG = "SettingsOnBoot";
    Boolean mSetupRunning = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for(int i = 0; i < procInfos.size(); i++)
        {
            if(procInfos.get(i).processName.equals("com.google.android.setupwizard") || (procInfos.get(i).processName.equals("com.cyanogenmod.setupwizard"))) {
                mSetupRunning = true;
            }
        }
        if(!mSetupRunning) {
             try {
                 settingsContext = context.createPackageContext("com.android.settings", 0);
             } catch (Exception e) {
                 Log.e(TAG, "Package not found", e);
             }
             SharedPreferences sharedpreferences = settingsContext.getSharedPreferences("com.android.settings_preferences", Context.MODE_PRIVATE);
             if(sharedpreferences.getBoolean("selinux", true)) {
                 CMDProcessor.runSuCommand("setenforce 1");
             } else if (!sharedpreferences.getBoolean("selinux", true)) {
                 CMDProcessor.runSuCommand("setenforce 0");
             }
        }
    }
}
