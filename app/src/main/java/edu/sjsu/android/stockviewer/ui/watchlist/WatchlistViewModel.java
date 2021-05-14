package edu.sjsu.android.stockviewer.ui.watchlist;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.util.ArrayList;

import edu.sjsu.android.stockviewer.MainActivity;
import edu.sjsu.android.stockviewer.WatchlistDB;
import edu.sjsu.android.stockviewer.WatchlistItem;

public class WatchlistViewModel extends ViewModel implements LoaderManager.LoaderCallbacks<Cursor> {

    private ArrayList<WatchlistItem> list;
    Context context;

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        return new CursorLoader(context, MainActivity.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        list = new ArrayList<>();
        String symbol;
        int type;

        while(data.moveToNext()) {
            symbol = data.getString(data.getColumnIndex(WatchlistDB.getSYMBOL()));
            type = data.getInt(data.getColumnIndex(WatchlistDB.getTYPE()));
            WatchlistItem item = new WatchlistItem(symbol, type);
            list.add(item);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    public ArrayList<WatchlistItem> getList() {
        return list;
    }
}