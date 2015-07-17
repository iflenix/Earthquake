package com.storm.earthquake;

import android.accounts.AccountManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by HOME on 18.06.2015.
 */
public class EarthquakeUpdateService extends IntentService {

    public static String TAG = "EARTHQUAKE_UPDATE_SERVICE";
    private Notification.Builder earthquakeNotificationBuilder;
    public static final int NOTIFICATION_ID = 100;

    private AlarmManager alarmManager;
    private PendingIntent alarmIntent;

    public EarthquakeUpdateService() {
        super("EarthquakeUpdateService");
    }

    public EarthquakeUpdateService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intentToFire = new Intent(EarthquakeAlarmReceiver.ACTION_REFRESH_EARTHQUAKE_ALARM);
        alarmIntent = PendingIntent.getBroadcast(this, 0, intentToFire, 0);
        earthquakeNotificationBuilder = new Notification.Builder(getApplicationContext());
        earthquakeNotificationBuilder.setSmallIcon(android.R.drawable.ic_dialog_map)
                .setAutoCancel(true)
                .setTicker("Earthquake Detected");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static String QUAKES_REFRESHED = "com.storm.earthquake.QUAKES_REFRESHED";

    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int updateFreq = Integer.parseInt(prefs.getString(preferences.PREF_UPDATE_FREQ, "60"));
        boolean autoUpdateChecked = prefs.getBoolean(preferences.PREF_AUTO_UPDATE, false);

        //updateTimer.cancel();
        if (autoUpdateChecked) {
            int alarmType = AlarmManager.ELAPSED_REALTIME_WAKEUP;
            long timeToRefresh = SystemClock.elapsedRealtime() + updateFreq * 60 * 1000;
            alarmManager.setInexactRepeating(alarmType, timeToRefresh, updateFreq * 60 * 1000, alarmIntent);
        } else
            alarmManager.cancel(alarmIntent);
        refreshEarthquakes();
        sendBroadcast(new Intent(QUAKES_REFRESHED));

        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);

        ComponentName earthquakeWidget = new ComponentName(context,EarthquakeListWidget.class);
        int[] appWidgetIds = widgetManager.getAppWidgetIds(earthquakeWidget);

        widgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_list_view);

    }

    private void addNewQuake(Quake _quake) {

        ContentResolver cr = getContentResolver();
        String where = EarthquakeProvider.KEY_DATE + " = " + _quake.getDate().getTime();
        Cursor query;
        query = cr.query(EarthquakeProvider.CONTENT_URI, null, where, null, null);
        if (query.getCount() == 0) {
            ContentValues cv = new ContentValues();
            cv.put(EarthquakeProvider.KEY_DATE, _quake.getDate().getTime());
            cv.put(EarthquakeProvider.KEY_DETAILS, _quake.getDetails());
            cv.put(EarthquakeProvider.KEY_LOCATION_LAT, _quake.getLocation().getLatitude());
            cv.put(EarthquakeProvider.KEY_LOCATION_LNG, _quake.getLocation().getLongitude());
            cv.put(EarthquakeProvider.KEY_LINK, _quake.getLink());
            cv.put(EarthquakeProvider.KEY_MAGNITUDE, _quake.getMagnitude());
            cv.put(EarthquakeProvider.KEY_SUMMARY, _quake.toString());
            broadcastNotification(_quake);
            cr.insert(EarthquakeProvider.CONTENT_URI, cv);
        }
        query.close();
    }

    private void broadcastNotification(Quake quake) {
        Intent startActivityIntent;
        startActivityIntent = new Intent(this, Earthquake.class);
        PendingIntent launchIntent = PendingIntent.getActivity(this, 0, startActivityIntent, 0);

        earthquakeNotificationBuilder.setContentIntent(launchIntent)
                .setWhen(quake.getDate().getTime())
                .setContentTitle("Magnitude: " + quake.getMagnitude())
                .setContentText(quake.getDetails());
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, earthquakeNotificationBuilder.build());

    }

    private void refreshEarthquakes() {

        URL url;

        try {
            String quakeFeed = getString(R.string.quake_feed);
            url = new URL(quakeFeed);
            URLConnection connection;
            connection = url.openConnection();
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream in = httpConnection.getInputStream();

                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document dom = db.parse(in);
                Element docEle = dom.getDocumentElement();

                NodeList nl = docEle.getElementsByTagName("entry");
                if (nl != null && nl.getLength() > 0) {
                    for (int i = 0; i < nl.getLength(); i++) {
                        Element entry = (Element) nl.item(i);
                        Element title = (Element) entry.getElementsByTagName("title").item(0);
                        if (title.getFirstChild().getNodeValue().equals("Data Feed Deprecated"))
                            continue;
                        Element g = (Element) entry.getElementsByTagName("georss:point").item(0);
                        Element when = (Element) entry.getElementsByTagName("updated").item(0);
                        Element link = (Element) entry.getElementsByTagName("link").item(0);

                        String details = title.getFirstChild().getNodeValue();
                        String hostname = "http://earthquake.usgs.gov";
                        String linkString = link.getAttribute("href");
                        String point = g.getFirstChild().getNodeValue();
                        String dt = when.getFirstChild().getNodeValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
                        Date qdate = new GregorianCalendar(0, 0, 0).getTime();
                        try {
                            qdate = sdf.parse(dt);
                        } catch (ParseException e) {
                            Log.d(TAG, "Date parse exception");
                        }

                        String[] location = point.split(" ");
                        Location l = new Location("dummyGPS");
                        l.setLatitude(Double.parseDouble(location[0]));
                        l.setLongitude(Double.parseDouble(location[1]));

                        String magnitudeString = details.split(" ")[1];
                        int end = magnitudeString.length() - 1;
                        double magnitude = Double.parseDouble(magnitudeString.substring(0, end));

                        details = details.split(",")[1].trim();
                        final Quake quake = new Quake(qdate, details, l, magnitude, linkString);

                        addNewQuake(quake);
                    }
                }
            }

        } catch (MalformedURLException e) {
            Log.d(TAG, "MalformedURLException");
        } catch (IOException e) {
            Log.d(TAG, "IOException");
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } finally {
            Intent updIntent = new Intent("com.storm.dataUpdated");

            Time time = new Time();
            time.setToNow();
            updIntent.putExtra("TIME_UPDATED", time.format("%H-%M-%S"));
            sendBroadcast(updIntent);
        }

    }
}
