package com.storm.earthquake;

import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Date;

/**
 * Created by StorM on 18.04.2015. TIME TOO FLY TO THE MOON
 */
public class EarthquakeListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        int layoutID = android.R.layout.simple_expandable_list_item_1;

        adapter = new SimpleCursorAdapter(getActivity(), layoutID, null,
                new String[]{EarthquakeProvider.KEY_SUMMARY}, new int[]{android.R.id.text1}, 0);
        setListAdapter(adapter);

        getLoaderManager().initLoader(0, null, this);

        refreshEarthquakes();

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cursor = cr.query(ContentUris.withAppendedId(EarthquakeProvider.CONTENT_URI, id), null, null, null, null);
        if (cursor.moveToFirst()) {

            Date date = new Date(cursor.getLong(cursor.getColumnIndex(EarthquakeProvider.KEY_DATE)));
            String Details = cursor.getString(cursor.getColumnIndex(EarthquakeProvider.KEY_DETAILS));
            double Magnitude = cursor.getDouble(cursor.getColumnIndex(EarthquakeProvider.KEY_MAGNITUDE));
            String linkString = cursor.getString(cursor.getColumnIndex(EarthquakeProvider.KEY_LINK));
            double lat = cursor.getDouble(cursor.getColumnIndex(EarthquakeProvider.KEY_LOCATION_LAT));
            double lng = cursor.getDouble(cursor.getColumnIndex(EarthquakeProvider.KEY_LOCATION_LNG));
            Location loc = new Location("db");
            loc.setLatitude(lat);
            loc.setLongitude(lng);

            Quake quake = new Quake(date, Details, loc, Magnitude, linkString);

            EarthquakeDialog infoDialog = EarthquakeDialog.newInstance(getActivity(), quake);
            infoDialog.show(getFragmentManager(), "QUAKE_INFO_DIALOG");

        }


        cursor.close();
    }


    public void refreshEarthquakes() {
        getLoaderManager().restartLoader(0, null, EarthquakeListFragment.this);
        Intent intent = new Intent(getActivity(), EarthquakeUpdateService.class);
        getActivity().startService(intent);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{EarthquakeProvider.KEY_ID, EarthquakeProvider.KEY_SUMMARY};

        Earthquake earthquakeActivity = (Earthquake) getActivity();
        String where = EarthquakeProvider.KEY_MAGNITUDE + " > " + earthquakeActivity.minimumMagnitude;
        return new CursorLoader(earthquakeActivity, EarthquakeProvider.CONTENT_URI, projection, where, null, null);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);

    }
}
