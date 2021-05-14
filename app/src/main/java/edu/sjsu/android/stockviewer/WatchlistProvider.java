package edu.sjsu.android.stockviewer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class WatchlistProvider extends ContentProvider {
    private WatchlistDB database;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int ret = database.delete(selection, selectionArgs);
        return ret;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = database.insert(values);
        //If record is added successfully
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        return null;
    }

    @Override
    public boolean onCreate() {
        database = new WatchlistDB(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor c = database.getAll();
        return c;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}