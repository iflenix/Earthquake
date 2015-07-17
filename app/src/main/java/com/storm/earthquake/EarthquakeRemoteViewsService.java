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
        return new EarthquakeRemoteViewsFactory(getApplicationContext());
    }

    class EarthquakeRemoteViewsFactory implements RemoteViewsFactory {

        private Context context;
        private Cursor quakeCursor;

        public EarthquakeRemoteViewsFactory(Context context) {
            this.context = context;

        }

        private Cursor executeQuery() {
            String[] projection = new String[] {
                    EarthquakeProvider.KEY_ID,
                    EarthquakeProvider.KEY_MAGNITUDE,
                    EarthquakeProvider.KEY_DETAILS
            };

            Context appContext = getApplicationContext();
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(appContext);

            int minimumMagnitude =
                    Integer.parseInt(prefs.getString(FragmentPreferences.PREF_MIN_MAG, "3"));

            String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + minimumMagnitude;

            return context.getContentResolver().query(EarthquakeProvider.CONTENT_URI,
                    projection, where, null, null);

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
            // Move the Cursor to the required index.
            quakeCursor.moveToPosition(position);

            // Extract the values for the current cursor row.
            int idIdx = quakeCursor.getColumnIndex(EarthquakeProvider.KEY_ID);
            int magnitudeIdx = quakeCursor.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE);
            int detailsIdx = quakeCursor.getColumnIndex(EarthquakeProvider.KEY_DETAILS);

            String id = quakeCursor.getString(idIdx);
            String magnitude = quakeCursor.getString(magnitudeIdx);
            String details = quakeCursor.getString(detailsIdx);

            // Create a new Remote Views object and use it to populate the
            // layout used to represent each earthquake in the list.
            RemoteViews rv = new RemoteViews(context.getPackageName(),
                    R.layout.quake_widget);

            rv.setTextViewText(R.id.widget_magnitude, magnitude);
            rv.setTextViewText(R.id.widget_details, details);

            // Create the fill-in Intent that adds the URI for the current item
            // to the template Intent.
            Intent fillInIntent = new Intent();
            Uri uri = Uri.withAppendedPath(EarthquakeProvider.CONTENT_URI, id);
            fillInIntent.setData(uri);

            rv.setOnClickFillInIntent(R.id.widget_magnitude, fillInIntent);
            rv.setOnClickFillInIntent(R.id.widget_details, fillInIntent);

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {

            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
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
