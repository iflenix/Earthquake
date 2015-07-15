package com.storm.earthquake;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by StorM on 27.05.2015.
 */
public class EarthquakeProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://com.storm.earthquakeprovider/earthquakes");

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_LOCATION_LAT = "latitude";
    public static final String KEY_LOCATION_LNG = "longitude";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_LINK = "link";

    public SQLiteDatabase earthquakeDatabase;

    EarthquakeDatabaseHelper dbHelper;

    @Override
    public boolean onCreate() {
        Context context = getContext();

        dbHelper = new EarthquakeDatabaseHelper(context,
                EarthquakeDatabaseHelper.DATABASE_NAME, null,
                EarthquakeDatabaseHelper.DATABASE_VERSION);
        return true;
    }

    private static final HashMap<String, String> SEARCH_PROJECTION_MAP;

    static {
        SEARCH_PROJECTION_MAP = new HashMap<String, String>();
        SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1, KEY_SUMMARY +
                " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SEARCH_PROJECTION_MAP.put("_id", KEY_ID +
                " AS " + "_id");
    }

    private static final int QUAKES = 1;
    private static final int QUAKE_ID = 2;
    private static final int SEARCH = 3;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.storm.earthquakeprovider", "earthquakes", QUAKES);
        uriMatcher.addURI("com.storm.earthquakeprovider", "earthquakes/#", QUAKE_ID);
        uriMatcher.addURI("com.storm.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
        uriMatcher.addURI("com.storm.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        uriMatcher.addURI("com.storm.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
        uriMatcher.addURI("com.storm.earthquakeprovider",
                SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
    }


    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                return "vnd.android.cursor.dir/vnd.storm.earthquake";
            case QUAKE_ID:
                return "vnd.android.cursor.item/vnd.storm.earthquake";
            case SEARCH:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported Uri");
        }
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(EarthquakeDatabaseHelper.DATABASE_TABLE);

        switch (uriMatcher.match(uri)) {
            case QUAKE_ID:
                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            case SEARCH:
                qb.appendWhere(KEY_SUMMARY + " LIKE \"%" +
                        uri.getPathSegments().get(1) + "%\"");
                qb.setProjectionMap(SEARCH_PROJECTION_MAP);
                break;
            default:
                break;
        }


        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = KEY_DATE;
        } else {
            orderBy = sortOrder;
        }

        Cursor c = qb.query(database,
                projection,
                selection, selectionArgs,
                null, null,
                orderBy);


        c.setNotificationUri(getContext().getContentResolver(), uri);

        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        earthquakeDatabase = dbHelper.getWritableDatabase();
        long insertedRowId = earthquakeDatabase.insert(EarthquakeDatabaseHelper.DATABASE_TABLE, "quake", values);

        if (insertedRowId > 0) {
            Uri resUri = ContentUris.withAppendedId(CONTENT_URI, insertedRowId);
            getContext().getContentResolver().notifyChange(resUri, null);
            return resUri;
        }
        return null;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        earthquakeDatabase = dbHelper.getWritableDatabase();

        int count;

        switch (uriMatcher.match(uri)) {
            case QUAKE_ID:
                selection = KEY_ID + " = " + uri.getPathSegments().get(1) + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
                count = earthquakeDatabase.delete(EarthquakeDatabaseHelper.DATABASE_TABLE, selection, selectionArgs);
                break;
            case QUAKES:
                count = earthquakeDatabase.delete(EarthquakeDatabaseHelper.DATABASE_TABLE, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri" + uri);

        }

        return count;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        earthquakeDatabase = dbHelper.getWritableDatabase();
        int count;

        switch (uriMatcher.match(uri)) {
            case QUAKE_ID:
                String segment = uri.getPathSegments().get(1);
                selection = KEY_ID + " = " + segment + (!TextUtils.isEmpty(selection) ? "AND (" + selection + ")" : "");
                count = earthquakeDatabase.update(EarthquakeDatabaseHelper.DATABASE_TABLE, values, selection, selectionArgs);
                break;
            case QUAKES:
                count = earthquakeDatabase.update(EarthquakeDatabaseHelper.DATABASE_TABLE, values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Uri" + uri);
        }

        return count;
    }

    private static class EarthquakeDatabaseHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "earthquakes.db";
        private static final String TAG = "EarthquakeProvider";
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_TABLE = "earthquakes";
        private static final String DATABASE_CREATE = "create table " + DATABASE_TABLE + " (" + KEY_ID + " integer primary key autoincrement, " +
                KEY_DATE + " INTEGER, " + KEY_DETAILS + " TEXT, " + KEY_SUMMARY + " TEXT, " + KEY_LOCATION_LAT + " FLOAT, " + KEY_LOCATION_LNG +
                " FLOAT, " + KEY_MAGNITUDE + " FLOAT, " + KEY_LINK + " TEXT );";

        private SQLiteDatabase earthquakesDB;


        public EarthquakeDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

            Log.w(TAG, "Upgrading DB");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);


        }
    }


}
