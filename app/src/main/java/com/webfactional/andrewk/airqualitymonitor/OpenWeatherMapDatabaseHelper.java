package com.webfactional.andrewk.airqualitymonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 */
public class OpenWeatherMapDatabaseHelper extends SQLiteOpenHelper
{
    private static final String DATABASE_NAME = "openweathermap";

    public static final String OZONELAYER_TABLE_COLUMN_ID = "_id";

    private static final String SQL_CREATE_TABLE_OZONELAYER = "CREATE TABLE " +
        OpenWeatherMapContentProvider.OZONELAYER_TABLE_NAME + " " +
        "(" +
        "  " + OZONELAYER_TABLE_COLUMN_ID + " INTEGER PRIMARY KEY," +
        "  time TEXT," +
        "  latitude TEXT," +
        "  longitude TEXT," +
        "  data REAL," +
        "  lastUpdateTime TEXT" +
        ")";

    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL(SQL_CREATE_TABLE_OZONELAYER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int i, int i1)
    {
        database.execSQL("DROP TABLE IF EXISTS " + OpenWeatherMapContentProvider.OZONELAYER_TABLE_NAME);
        onCreate(database);
    }

    public OpenWeatherMapDatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
    }
}