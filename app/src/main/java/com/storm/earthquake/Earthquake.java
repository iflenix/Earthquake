package com.storm.earthquake;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Earthquake extends Activity {

    public int minimumMagnitude = 0;
    public boolean autoUpdateChecked = false;
    public int updateFreq = 0;

    TabListener<EarthquakeListFragment> listTabListener;
    TabListener<EarthquakeMapFragment> mapTabListener;

    private static String ACTION_BAR_INDEX = "ACTION_BAR_INDEX";

    TextView updTextView;


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

        updTextView = (TextView) findViewById(R.id.update_time_textview);
        registerForContextMenu(updTextView);

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
    private static final int MENU_TEST = Menu.FIRST + 3;


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());

        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchableInfo);

       /* SubMenu sub = menu.addSubMenu(0, 0, Menu.NONE, "Submenu");
        sub.setHeaderIcon(R.drawable.abc_btn_radio_to_on_mtrl_000);
        sub.setIcon(R.drawable.abc_list_selector_holo_light);

        sub.add(0, 0, 0, "SubmenuItem1");
        sub.add(0, 0, 0, "SubmenuItem2");*/

        //menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);
        // menu.add(Menu.NONE, MENU_TEST, Menu.NONE, "Test").setCheckable(true);

        //menu.findItem(MENU_TEST).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS).setIcon(R.drawable.abc_ic_menu_paste_mtrl_am_alpha);

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
            case R.id.menu_refresh:
                Intent intent = new Intent(this, EarthquakeUpdateService.class);
                startService(intent);
                return true;
            case R.id.menu_preferences:
                Intent i = new Intent(this, FragmentPreferences.class);
                startActivityForResult(i, SHOW_PREFERENCES);
                return true;
            case R.id.menu_fullscreen:
                if (item.isChecked()) {
                    getCurrentFocus().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    item.setChecked(false);
                } else {
                    getCurrentFocus().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN);

                    item.setChecked(true);
                }
                return true;
            case R.id.menu_dialog:
/*                Dialog dialog = new Dialog(Earthquake.this);
                dialog.setTitle("Dialog Title");
                dialog.setContentView(android.R.layout.select_dialog_item);
                dialog.show();*/
                showDialog(1);
                return true;


            default:
                return false;

        }

        //return super.onOptionsItemSelected(item);
    }

    private final int MENU_COLOR_RED = 1;
    private final int MENU_COLOR_GREEN = 2;
    private final int MENU_COLOR_BLUE = 3;

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        switch (v.getId()) {
            case R.id.update_time_textview:
                menu.add(0, MENU_COLOR_BLUE, 0, "Blue");
                menu.add(0, MENU_COLOR_GREEN, 0, "Green");
                menu.add(0, MENU_COLOR_RED, 0, "Red");
                break;
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_COLOR_BLUE:
                updTextView.setTextColor(Color.BLUE);
                break;
            case MENU_COLOR_GREEN:
                updTextView.setTextColor(Color.GREEN);
                break;
            case MENU_COLOR_RED:
                updTextView.setTextColor(Color.RED);
                break;

        }
        return super.onContextItemSelected(item);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == 1) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Title");
            adb.setMessage("Dialog Message");
            adb.setIcon(android.R.drawable.ic_dialog_alert);
            adb.setPositiveButton("OK!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });

            adb.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        if (id == 1) {
            AlertDialog alertDialog = (AlertDialog) dialog;
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            alertDialog.setMessage(sdf.format(new Date(System.currentTimeMillis())));
        }

        super.onPrepareDialog(id, dialog);
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


