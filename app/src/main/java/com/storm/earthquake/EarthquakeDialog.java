package com.storm.earthquake;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by HOME on 06.07.2015. at home
 */
public class EarthquakeDialog extends DialogFragment {
    private static String DIALOG_STRING = "DIALOG_STRING";

    public static EarthquakeDialog newInstance(Context context, Quake quake) {
        EarthquakeDialog fragment = new EarthquakeDialog();
        Bundle args = new Bundle();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.US);
        String dateString = sdf.format(quake.getDate());
        String quakeString = dateString + "\n" + "Magnitude: " + quake.getMagnitude() + "\n" + quake.getDetails() +
                "\n" + quake.getLink();
        args.putString(DIALOG_STRING, quakeString);
        fragment.setArguments(args);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.quake_details, container, false);
        String title = getArguments().getString(DIALOG_STRING);
        TextView tv = (TextView) v.findViewById(R.id.quakeDetailsTextView);
        tv.setText(title);
        return v;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle("Earthquake Details");
        return dialog;
    }
}
