package com.storm.earthquake;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;


public class preferences extends PreferenceActivity {

    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
    public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userpreferences);
    }

    /*CheckBox autoUpdate;
    Spinner updateFreqSpinner;
    Spinner magnitudeSpinner;

    public static final String USER_PREFERENCE = "USER_PREFERENCE";
    public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
    public static final String PREF_MIN_MAG_INDEX = "PREF_MIN_MAG_INDEX";
    public static final String PREF_UPDATE_FREQ_INDEX = "PREF_UPDATE_FREQ_INDEX";

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferences);
        updateFreqSpinner = (Spinner) findViewById(R.id.spinner_update_freq);
        magnitudeSpinner = (Spinner) findViewById(R.id.spinner_quake_mag);
        autoUpdate = (CheckBox) findViewById(R.id.autoUpdateCheckbox);

        populateSpinners();

        Context context = getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        updateUIFromPreferences();

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePreferences();
                preferences.this.setResult(RESULT_OK);
                finish();


            }
        });

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                preferences.this.setResult(RESULT_CANCELED);
                finish();
            }
        });

    }

    private void savePreferences() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_AUTO_UPDATE,autoUpdate.isChecked());
        editor.putInt(PREF_MIN_MAG_INDEX,magnitudeSpinner.getSelectedItemPosition());
        editor.putInt(PREF_UPDATE_FREQ_INDEX, updateFreqSpinner.getSelectedItemPosition());
        editor.apply();

    }

    private void updateUIFromPreferences() {
        boolean autoUpdateChecked = prefs.getBoolean(PREF_AUTO_UPDATE, false);
        int updateFreqIndex = prefs.getInt(PREF_UPDATE_FREQ_INDEX, 2);
        int minMagIndex = prefs.getInt(PREF_MIN_MAG_INDEX, 0);
        updateFreqSpinner.setSelection(updateFreqIndex);
        magnitudeSpinner.setSelection(minMagIndex);
        autoUpdate.setChecked(autoUpdateChecked);
    }

    private void populateSpinners() {
        ArrayAdapter<CharSequence> fAdapter;
        fAdapter = ArrayAdapter.createFromResource(this, R.array.update_freq_values, android.R.layout.simple_spinner_item);
        int spinner_dd_item = android.R.layout.simple_spinner_dropdown_item;
        fAdapter.setDropDownViewResource(spinner_dd_item);
        updateFreqSpinner.setAdapter(fAdapter);

        ArrayAdapter<CharSequence> mAdapter;
        mAdapter = ArrayAdapter.createFromResource(this, R.array.magnitude_options, android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        magnitudeSpinner.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
}
