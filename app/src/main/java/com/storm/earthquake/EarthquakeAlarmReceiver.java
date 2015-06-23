package com.storm.earthquake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by HOME on 23.06.2015.
 */
public class EarthquakeAlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_REFRESH_EARTHQUAKE_ALARM = "com.storm.earthquake.ACTION_REFRESH_EARTHQUAKE_ALARM";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent startServiceIntent = new Intent(context, EarthquakeUpdateService.class);
        context.startService(startServiceIntent);
    }
}
