package com.storm.earthquake;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

/**
 * Created by StorM on 16.07.2015.
 */
public class EarthquakeRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return null;
    }

    class EarthquakeRemoteViewsFactory implements RemoteViewsFactory {

        private Context context;
        private Cursor quakeCursor;

        public EarthquakeRemoteViewsFactory(Context context) {
            this.context = context;

        }

        private Cursor executeQuery() {
            String[] projection = new String[]{EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_MAGNITUDE, EarthquakeProvider.KEY_DETAILS};
            ContentResolver cr = context.getContentResolver();
            Context context = getApplicationContext();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String selection = EarthquakeProvider.KEY_MAGNITUDE + " > " + preferences.getString(FragmentPreferences.PREF_MIN_MAG, "3");

            return cr.query(EarthquakeProvider.CONTENT_URI, projection, selection, null, null);

        }

        @Override
        public void onCreate() {
            quakeCursor = executeQuery();
        }

        @Override
        public void onDataSetChanged() {
            quakeCursor = executeQuery();

        }

        @Override
        public void onDestroy() {
            quakeCursor.close();

        }

        @Override
        public int getCount() {
            if (quakeCursor != null) {
                return quakeCursor.getCount();
            } else
                return 0;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            quakeCursor.moveToPosition(position);
            RemoteViews earthquakeRemoteView = new RemoteViews(context.getPackageName(), R.layout.quake_collection_widget);

            earthquakeRemoteView.setTextViewText(R.id.widget_details, quakeCursor.getString(quakeCursor.getColumnIndexOrThrow(EarthquakeProvider.KEY_DETAILS)));
            earthquakeRemoteView.setTextViewText(R.id.widget_magnitude, quakeCursor.getString(quakeCursor.getColumnIndexOrThrow(EarthquakeProvider.KEY_MAGNITUDE)));

            Intent fillInIntent = new Intent();
            Uri uri = Uri.withAppendedPath(EarthquakeProvider.CONTENT_URI,quakeCursor.getString(quakeCursor.getColumnIndex(EarthquakeProvider.KEY_ID)));
            fillInIntent.setData(uri);

            earthquakeRemoteView.setOnClickFillInIntent(R.id.widget_magnitude,fillInIntent);
            earthquakeRemoteView.setOnClickFillInIntent(R.id.widget_details,fillInIntent);

            return earthquakeRemoteView;
        }

        @Override
        public RemoteViews getLoadingView() {

            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            if (quakeCursor != null) {
                return quakeCursor.getLong(quakeCursor.getColumnIndex(EarthquakeProvider.KEY_ID));
            } else
                return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
