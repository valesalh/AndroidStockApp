package edu.sjsu.android.stockviewer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class WatchlistDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "watchlistDB";
    private static final int VERSION = 1;
    protected static final String TABLE_NAME = "watchlist";
    protected static final String ID = "_id";
    protected static final String SYMBOL = "symbol";
    protected static final String TYPE = "type";
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " ( " +
                    SYMBOL + " TEXT NOT NULL primary key, " +
                    TYPE + " INTEGER " +
                    ");";

    public WatchlistDB(@Nullable Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insert(ContentValues contentValues) {
        SQLiteDatabase database = getWritableDatabase();
        long ret = -1;
        try {
            ret = database.insert(TABLE_NAME, null, contentValues);
        } catch (SQLiteConstraintException e) {
            return ret;
        }
        return ret;
    }

    public int delete(String whereClause, String[] whereArgs) {
        SQLiteDatabase database = getWritableDatabase();
        int ret = database.delete(TABLE_NAME, whereClause , whereArgs);
        return ret;
    }

    public Cursor getAll() {
        SQLiteDatabase database = getWritableDatabase();
        Cursor c =  database.query(TABLE_NAME,
                new String[]{SYMBOL, TYPE},
                null, null, null, null, null);
        return c;
    }

    public static String getSYMBOL() {
        return SYMBOL;
    }

    public static String getTYPE() {
        return TYPE;
    }
}
