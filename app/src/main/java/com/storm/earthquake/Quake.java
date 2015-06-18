package com.storm.earthquake;

import android.location.Location;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by StorM on 18.04.2015.
 */
public class Quake {
    private double magnitude;
    private Date date;
    private String link;
    private String details;
    private Location location;

    public double getMagnitude() {
        return magnitude;
    }

    public Date getDate() {
        return date;
    }

    public String getLink() {
        return link;
    }

    public String getDetails() {
        return details;
    }

    public Location getLocation() {
        return location;
    }

    public Quake(Date _d, String _det, Location _loc, double _mag, String _link) {
        date = _d;
        details = _det;
        location = _loc;
        magnitude = _mag;
        link = _link;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd - HH.mm");
        String dateString = sdf.format(date);

        return dateString + ": Mag:" + magnitude + ": " + details;

    }
}
