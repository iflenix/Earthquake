package com.storm.earthquake;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class EarthquakeMapFragment extends Fragment {

    MapView mMapView;
    private GoogleMap googleMap;
    ArrayList<MarkerOptions> markerPoints = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.map_fragment, container,
                false);
        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        /*try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        googleMap = mMapView.getMap();

        getMarkers();
        drawMarkers();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(markerPoints.get(0).getPosition()).zoom(5).build();
        googleMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition));

        return v;
    }

    private void getMarkers() {
        markerPoints.clear();
        ContentResolver cr = getActivity().getContentResolver();
        Context context = getActivity().getApplicationContext();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String minMagnitude = preferences.getString(FragmentPreferences.PREF_MIN_MAG, "0");


        String[] projection = new String[]{EarthquakeProvider.KEY_SUMMARY,
                EarthquakeProvider.KEY_LOCATION_LAT, EarthquakeProvider.KEY_LOCATION_LNG};
        String selection = EarthquakeProvider.KEY_MAGNITUDE + " > " + minMagnitude;
        Cursor cursor = cr.query(EarthquakeProvider.CONTENT_URI, projection, selection, null, null);
        while (cursor.moveToNext()) {
            double latitude, longitude;
            String summary;
            latitude = cursor.getDouble(cursor.getColumnIndex(EarthquakeProvider.KEY_LOCATION_LAT));
            longitude = cursor.getDouble(cursor.getColumnIndex(EarthquakeProvider.KEY_LOCATION_LNG));
            summary = cursor.getString(cursor.getColumnIndex(EarthquakeProvider.KEY_SUMMARY));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(latitude, longitude)).title(summary);
            markerPoints.add(markerOptions);
        }
        cursor.close();
    }

    private void drawMarkers() {
        for (MarkerOptions marker : markerPoints) {
            googleMap.addMarker(marker);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
}