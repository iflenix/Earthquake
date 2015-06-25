package com.storm.earthquake;

import android.app.ActionBar;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


public class Earthquake extends Activity {

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    TabListener<EarthquakeListFragment> listTabListener;
    TabListener<EarthquakeMapFragment> mapTabListener;

    private static String ACTION_BAR_INDEX = "ACTION_BAR_INDEX";


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;
        if (!tabletLayout) {
            int actionBarIndex = getActionBar().getSelectedTab().getPosition();
            SharedPreferences.Editor editor = getPreferences(Activity.MODE_PRIVATE).edit();
            editor.putInt(ACTION_BAR_INDEX, actionBarIndex);
            editor.apply();

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (mapTabListener.fragment != null) {
                ft.detach(mapTabListener.fragment);
            }
            if (listTabListener.fragment != null) {
                ft.detach(listTabListener.fragment);
            }
            ft.commit();
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;
        if (!tabletLayout) {
            SharedPreferences sp = getPreferences(MODE_PRIVATE);
            int actionBarIndex = sp.getInt(ACTION_BAR_INDEX, 0);
            getActionBar().setSelectedNavigationItem(actionBarIndex);
        }
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;
        if (!tabletLayout) {
            listTabListener.fragment = getFragmentManager()
                    .findFragmentByTag(EarthquakeListFragment.class.getName());
            mapTabListener.fragment = getFragmentManager()
                    .findFragmentByTag(EarthquakeMapFragment.class.getName());
            SharedPreferences sp = getPreferences(Activity.MODE_PRIVATE);
            int actionBarIndex = sp.getInt(ACTION_BAR_INDEX, 0);
            getActionBar().setSelectedNavigationItem(actionBarIndex);
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_earthquake);
        updateFromPreferences();

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

        SearchView searchView = (SearchView) findViewById(R.id.searchView);
        searchView.setSearchableInfo(searchableInfo);
        searchView.setSubmitButtonEnabled(true);

        registerReceiver(uiUpdated, new IntentFilter("com.storm.dataUpdated"));

        ActionBar actionBar = getActionBar();
        View fragmentContainer = findViewById(R.id.EarthquakeFragmentContainer);
        boolean tabletLayout = fragmentContainer == null;

        if (!tabletLayout) {
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
            ActionBar.Tab listTab = actionBar.newTab();
            listTabListener = new TabListener<>(this, R.id.EarthquakeFragmentContainer, EarthquakeListFragment.class);

            listTab.setText("List").setContentDescription("List of earthquakes").setTabListener(listTabListener);
            actionBar.addTab(listTab);

            ActionBar.Tab mapTab = actionBar.newTab();
            mapTabListener = new TabListener<>(this, R.id.EarthquakeFragmentContainer, EarthquakeMapFragment.class);
            ;
            mapTab.setText("Map").setContentDescription("Map of earthquakes").setTabListener(mapTabListener);

            actionBar.addTab(mapTab);
        }

    }

    private BroadcastReceiver uiUpdated = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TextView tv = (TextView) findViewById(R.id.update_time_textview);
            String updString = "Updated: " + intent.getExtras().getString("TIME_UPDATED");
            tv.setText(updString);
            ActionBar actionBar = getActionBar();

            actionBar.setSubtitle(updString);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SHOW_PREFERENCES) {
            updateFromPreferences();
            startService(new Intent(this, EarthquakeUpdateService.class));
           /* FragmentManager fm = getFragmentManager();
            final EarthquakeListFragment earthquakeList = (EarthquakeListFragment) fm.findFragmentById(R.id.EarthquakeListFragment);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    earthquakeList.refreshEarthquakes();
                }
            });
            t.start();*/
        }
    }

    private void updateFromPreferences() {

        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        minimumMagnitude = Integer.parseInt(prefs.getString(FragmentPreferences.PREF_MIN_MAG, "3"));
        updateFreq = Integer.parseInt(prefs.getString(FragmentPreferences.PREF_UPDATE_FREQ, "60"));
        autoUpdateChecked = prefs.getBoolean(preferences.PREF_AUTO_UPDATE, false);
/*
        FragmentManager fm = getFragmentManager();
        Fragment listFragment = fm.findFragmentById(R.id.EarthquakeListFragment);
        listFragment.getLoaderManager().restartLoader(0, null, (LoaderManager.LoaderCallbacks<? extends Object>) listFragment);
*/

    }

    private static final int MENU_PREFERENCES = Menu.FIRST + 1;
    private static final int MENU_UPDATE = Menu.FIRST + 2;


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_earthquake, menu);
        menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);

        return true;
    }

    private static final int SHOW_PREFERENCES = 1;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case (MENU_PREFERENCES): {
                Intent i = new Intent(this, FragmentPreferences.class);
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
            }
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onTestButtonClick(View v) {
    /*
    Тестирование настроек активности, не общих настроек
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        SharedPreferences activityPrefs = getPreferences(Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = activityPrefs.edit();
        editor.putString("Key", "value");
        editor.apply();
        if (sharedPrefs.getBoolean("PREF_TEST_CHECKBOX", false))
            //Toast.makeText(this,sharedPrefs.getString("TEST_TEXT","No text"),Toast.LENGTH_LONG).show();
            Toast.makeText(this, activityPrefs.getString("Key", "No text"), Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Unchecked", Toast.LENGTH_SHORT).show();*/


        File file = getDir("", MODE_PRIVATE);
        Toast.makeText(this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        File ringtonesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_RINGTONES);
        Toast.makeText(this, ringtonesDir.getAbsolutePath(), Toast.LENGTH_LONG).show();


    }

    public void onDownloadButtonClick(View v) {


        final DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse("https://www.dropbox.com/s/kwvoqypbt3wv9zm/Thinking_in_Java_%284th_Edition%29_RUS.djvu?dl=1");
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //request.setDestinationUri(Uri.fromFile()
        final long dwreference = dm.enqueue(request);


        BroadcastReceiver downloadStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                    if (reference == dwreference) {
                        /*
                        Uri fileUri = dm.getUriForDownloadedFile(reference);
                        String downloadPath = fileUri.toString();
                        Toast.makeText(context,downloadPath,Toast.LENGTH_LONG).show();*/

                        DownloadManager.Query downloadQuery = new DownloadManager.Query();
                        downloadQuery.setFilterById(reference);
                        Cursor myDownload = dm.query(downloadQuery);
                        if (myDownload.moveToFirst()) {
                            int filenameIdx = myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
                            int fileUriIdx = myDownload.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                            String filename = myDownload.getString(filenameIdx);
                            String fileUri = myDownload.getString(fileUriIdx);

                            Toast.makeText(context, fileUri, Toast.LENGTH_LONG).show();


                        }
                        myDownload.close();

                    }
                {
                }
            }
        };

        IntentFilter dwnldFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(downloadStateReceiver, dwnldFilter);

    }

    public static class TabListener<T extends Fragment> implements ActionBar.TabListener {
        private Fragment fragment;
        private Activity activity;
        private Class<T> fragmentClass;
        private int fragmentContainer;


        public TabListener(Activity activity, int fragmentContainer, Class<T> fragmentClass) {
            this.activity = activity;
            this.fragmentContainer = fragmentContainer;
            this.fragmentClass = fragmentClass;
        }


        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            if (fragment == null) {
                String fragmentName = fragmentClass.getName();
                fragment = Fragment.instantiate(activity, fragmentName);
                fragmentTransaction.add(fragmentContainer, fragment, fragmentName);
            } else
                fragmentTransaction.attach(fragment);

        }

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            if (fragment != null) {
                fragmentTransaction.detach(fragment);
            }

        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
            if (fragment != null) {
                fragmentTransaction.attach(fragment);
            }

        }
    }

}


