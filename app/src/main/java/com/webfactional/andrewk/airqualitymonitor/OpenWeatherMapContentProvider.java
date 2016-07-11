package com.webfactional.andrewk.airqualitymonitor;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 *
 */
public class OpenWeatherMapContentProvider extends ContentProvider
{
    private static final String NAMESPACE = "com.webfactional.andrewk.airqualitymonitor";

    private static final String AUTHORITY = NAMESPACE + ".OpenWeatherMapContentProvider";

    public static final String OZONELAYER_TABLE_NAME = "ozonelayer";

    public static final Uri CONTENT_URI
        = Uri.parse("content://" + AUTHORITY + "/" + OZONELAYER_TABLE_NAME);

    public static final int OZONELAYER_URI_ID_ALL = 1;
    public static final int OZONELAYER_URI_ID_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static
    {
        uriMatcher.addURI(AUTHORITY, OZONELAYER_TABLE_NAME, OZONELAYER_URI_ID_ALL);
        uriMatcher.addURI(AUTHORITY, OZONELAYER_TABLE_NAME + "/#", OZONELAYER_URI_ID_ID);
    }

    OpenWeatherMapDatabaseHelper databaseHelper;

    @Override
    public boolean onCreate()
    {
        databaseHelper = new OpenWeatherMapDatabaseHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
    {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(OZONELAYER_TABLE_NAME);

        int uriType = uriMatcher.match(uri);

        switch (uriType) {
            case OZONELAYER_URI_ID_ID:
                queryBuilder.appendWhere(OpenWeatherMapDatabaseHelper.OZONELAYER_TABLE_COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            case OZONELAYER_URI_ID_ALL:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        Cursor cursor = queryBuilder.query(
            databaseHelper.getReadableDatabase(),
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder);

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri)
    {
        int uriType = uriMatcher.match(uri);

        switch (uriType) {
            case OZONELAYER_URI_ID_ID:
                return "vnd.android.cursor.item/vnd." + NAMESPACE + "." + OZONELAYER_TABLE_NAME;

            case OZONELAYER_URI_ID_ALL:
                return "vnd.android.cursor.dir/vnd." + NAMESPACE + "." + OZONELAYER_TABLE_NAME;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values)
    {
        int uriType = uriMatcher.match(uri);

        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        long id = 0;

        switch (uriType) {
            case OZONELAYER_URI_ID_ALL:
                id = database.insert(OZONELAYER_TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return Uri.parse(OZONELAYER_TABLE_NAME + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs)
    {
        int uriType = uriMatcher.match(uri);

        SQLiteDatabase sqlDB = databaseHelper.getWritableDatabase();

        int rowsDeleted = 0;

        switch (uriType)
        {
            case OZONELAYER_URI_ID_ALL:

                rowsDeleted = sqlDB.delete(OZONELAYER_TABLE_NAME,
                    selection,
                    selectionArgs);

            break;
            case OZONELAYER_URI_ID_ID:

                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection))
                {
                    rowsDeleted = sqlDB.delete(OZONELAYER_TABLE_NAME,
                            OpenWeatherMapDatabaseHelper.OZONELAYER_TABLE_COLUMN_ID + "=" + id,
                            null);
                }
                else
                {
                    rowsDeleted = sqlDB.delete(OZONELAYER_TABLE_NAME,
                            OpenWeatherMapDatabaseHelper.OZONELAYER_TABLE_COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }

            break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs)
    {
        int uriType = uriMatcher.match(uri);

        SQLiteDatabase database = databaseHelper.getWritableDatabase();

        int rowsUpdated = 0;

        switch (uriType)
        {
            case OZONELAYER_URI_ID_ALL:

                rowsUpdated = database.update(OZONELAYER_TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);

            break;
            case OZONELAYER_URI_ID_ID:

                String id = uri.getLastPathSegment();

                if (TextUtils.isEmpty(selection))
                {
                    rowsUpdated =
                        database.update(OZONELAYER_TABLE_NAME,
                            values,
                            OpenWeatherMapDatabaseHelper.OZONELAYER_TABLE_COLUMN_ID + "=" + id,
                            null);
                }
                else
                {
                    rowsUpdated =
                        database.update(OZONELAYER_TABLE_NAME,
                            values,
                            OpenWeatherMapDatabaseHelper.OZONELAYER_TABLE_COLUMN_ID + "=" + id
                                + " and "
                                + selection,
                            selectionArgs);
                }

            break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return rowsUpdated;
    }
}