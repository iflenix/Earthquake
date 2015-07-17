package com.storm.earthquake;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by StorM on 16.07.2015.
 */
public class EarthquakeListWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
       /// super.onUpdate(context, appWidgetManager, appWidgetIds);

        for (int appWidgetId:appWidgetIds) {
            Intent intent = new Intent(context,EarthquakeRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            RemoteViews views = new RemoteViews(context.getPackageName(),R.layout.quake_collection_widget);
            views.setRemoteAdapter(R.id.widget_list_view, intent);
            views.setEmptyView(R.id.widget_list_view,R.id.widget_empty_text);

            Intent templateIntent = new Intent(context,Earthquake.class);
            PendingIntent templatePendingIntent = PendingIntent.getActivity(context,0,templateIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list_view,templatePendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId,views);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }
}
