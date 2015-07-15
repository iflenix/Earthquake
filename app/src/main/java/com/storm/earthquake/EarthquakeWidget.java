package com.storm.earthquake;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

/**
 * Created by StorM on 15.07.2015.
 */
public class EarthquakeWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        updateQuake(context,appWidgetManager,appWidgetIds);
        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    public void updateQuake(Context context, AppWidgetManager appWidgetManager, int[] appWidgets) {
        ContentResolver cr = context.getContentResolver();
        String[] projection = new String[]{EarthquakeProvider.KEY_SUMMARY, EarthquakeProvider.KEY_MAGNITUDE, EarthquakeProvider.KEY_DATE};

        Cursor cursor = cr.query(EarthquakeProvider.CONTENT_URI, projection, null, new String[]{EarthquakeProvider.KEY_DATE}, null);

        String magnitde = "--";
        String details = "Unknown";

        if (cursor != null) {
            cursor.moveToFirst();
            magnitde = cursor.getString(cursor.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE));
            details = cursor.getString(cursor.getColumnIndex(EarthquakeProvider.KEY_DETAILS));
        }

        cursor.close();

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),R.layout.quake_widget);
        remoteViews.setTextViewText(R.id.widget_details,details);
        remoteViews.setTextViewText(R.id.widget_magnitude,magnitde);

        appWidgetManager.updateAppWidget(appWidgets,remoteViews);

    }

    public void updateQuake(Context context) {
        ComponentName thisWidget = new ComponentName(context, EarthquakeWidget.class);
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = widgetManager.getAppWidgetIds(thisWidget);

        updateQuake(context, widgetManager, widgetIds);


    }


}
