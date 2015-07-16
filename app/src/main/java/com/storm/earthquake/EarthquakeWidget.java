package com.storm.earthquake;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;

/**
 * Created by StorM on 15.07.2015.
 */
public class EarthquakeWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateQuake(context, appWidgetManager, appWidgetIds);
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Intent intent = new Intent(context,Earthquake.class);
        PendingIntent mainActivityLaunch = PendingIntent.getActivity(context,0,intent,0);

        RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.quake_widget);

        views.setOnClickPendingIntent(R.id.widget_magnitude,mainActivityLaunch);
        views.setOnClickPendingIntent(R.id.widget_details,mainActivityLaunch);

        appWidgetManager.updateAppWidget(appWidgetIds,views);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(EarthquakeUpdateService.QUAKES_REFRESHED)) {
            updateQuake(context);
        }


    }

    public void updateQuake(Context context, AppWidgetManager appWidgetManager, int[] appWidgets) {
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{EarthquakeProvider.KEY_DETAILS, EarthquakeProvider.KEY_MAGNITUDE};

        Cursor cursor = cr.query(EarthquakeProvider.CONTENT_URI, projection, null, null, EarthquakeProvider.KEY_DATE);

        String magnitde = "--";
        String details = "Unknown";
        String updatedTime = "hh:mm";

        if (cursor != null) {
            cursor.moveToLast();
            magnitde = cursor.getString(cursor.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE));
            details = cursor.getString(cursor.getColumnIndex(EarthquakeProvider.KEY_DETAILS));
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            updatedTime = sdf.format(SimpleDateFormat.getDateTimeInstance().getCalendar().getTime());
        }

        cursor.close();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.quake_widget);
        remoteViews.setTextViewText(R.id.widget_details,details);
        remoteViews.setTextViewText(R.id.widget_magnitude,magnitde);
        remoteViews.setTextViewText(R.id.widget_updated_time,updatedTime);

        appWidgetManager.updateAppWidget(appWidgets,remoteViews);

    }

    public void updateQuake(Context context) {
        ComponentName thisWidget = new ComponentName(context, EarthquakeWidget.class);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = widgetManager.getAppWidgetIds(thisWidget);

        updateQuake(context, widgetManager, widgetIds);


    }


}
