package com.webfactional.andrewk.airqualitymonitor;

import android.app.Service;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 *
 */
public class OzoneLayerDataService extends Service implements Loader.OnLoadCompleteListener<Cursor>
{
    private static String LOG_TAG = "OzoneLayerDataService";

    private IBinder binder = new OzoneLayerDataServiceBinder();

    private CursorLoader cursorLoader = null;

    @Nullable
    @Override
    public IBinder onBind(final Intent intent)
    {
        getData(intent);

        return binder;
    }

    private void getData(Intent intent)
    {
        String latitude = intent.getStringExtra("latitude");
        String longitude = intent.getStringExtra("longitude");
        Messenger ozoneLayerDataMessenger = (Messenger)intent.getExtras().get("ozoneLayerDataMessenger");
        boolean isNetworkAvailable = intent.getBooleanExtra("isNetworkAvailable", false);

        if (isNetworkAvailable)
        {
            getDataFromInternet(latitude, longitude, ozoneLayerDataMessenger);
        }
        else
        {
            getDataFromCache(latitude, longitude, ozoneLayerDataMessenger);
        }
    }

    private Messenger ozoneLayerDataMessenger;

    private void getDataFromCache(
        String latitude,
        String longitude,
        Messenger ozoneLayerDataMessenger)
    {
        this.ozoneLayerDataMessenger = ozoneLayerDataMessenger;

        String[] columns = new String[] {
            OpenWeatherMapDatabaseHelper.OZONELAYER_TABLE_COLUMN_ID,
            "time",
            "latitude",
            "longitude",
            "data",
            "lastUpdateTime",
        };

        String where = "latitude = ? AND longitude = ?";

        String[] whereArgs = new String[] {
            latitude,
            longitude,
        };

        cursorLoader = new CursorLoader(getApplicationContext(), OpenWeatherMapContentProvider.CONTENT_URI, columns, where, whereArgs, "lastUpdateTime DESC");
        cursorLoader.registerListener(666, this);
        cursorLoader.startLoading();
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor)
    {
        //Cursor cursor = getContentResolver().query(OpenWeatherMapContentProvider.CONTENT_URI, columns, where, whereArgs, "lastUpdateTime DESC");

        if (null == cursor)
        {
            Log.e("AirQualityMonitor", "Cursor is null.");
        }
        else if (cursor.getCount() < 1)
        {
            Log.e("AirQualityMonitor", "Cursor count is zero.");
        }
        else
        {
            cursor.moveToNext();

            int dataColumnIndex = cursor.getColumnIndex("data");
            double data = cursor.getDouble(dataColumnIndex);

            int latitudeColumnIndex = cursor.getColumnIndex("latitude");
            String latitude = cursor.getString(latitudeColumnIndex);

            int longitudeColumnIndex = cursor.getColumnIndex("longitude");
            String longitude = cursor.getString(longitudeColumnIndex);

            LocationSerializable location = new LocationSerializable();
            location.setLatitude(Double.parseDouble(latitude));
            location.setLongitude(Double.parseDouble(longitude));

            int timeColumnIndex = cursor.getColumnIndex("time");
            String time = cursor.getString(timeColumnIndex);

            OzoneLayerData ozoneLayerData = new OzoneLayerData();
            ozoneLayerData.setLocation(location);
            ozoneLayerData.setData(data);
            ozoneLayerData.setTime(time);

            Message message = Message.obtain();
            message.obj = ozoneLayerData;

            try
            {
                ozoneLayerDataMessenger.send(message);
            }
            catch (RemoteException ex)
            {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if (null != cursorLoader)
        {
            cursorLoader.unregisterListener(this);
            cursorLoader.cancelLoad();
            cursorLoader.stopLoading();
            ozoneLayerDataMessenger = null;
        }
    }

    private void getDataFromInternet(
        final String latitude,
        final String longitude,
        final Messenger ozoneLayerDataMessenger)
    {
        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                String location = latitude + "," + longitude;
                String dateTime = "current";
                String apiKey = "660b7db8b5310c04c657ee811e21c280";

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://api.openweathermap.org/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                IOpenWeatherMapApi service = retrofit.create(IOpenWeatherMapApi.class);

                Call<OzoneLayerData> ozoneLayerDataCall = service.getOzoneLayerData(location, dateTime, apiKey);

                try
                {
                    Response<OzoneLayerData> ozoneLayerDataResponse = ozoneLayerDataCall.execute();

                    OzoneLayerData ozoneLayerData = ozoneLayerDataResponse.body();

                    Message message = Message.obtain();
                    message.obj = ozoneLayerData;

                    ozoneLayerDataMessenger.send(message);

                    ContentValues values = new ContentValues();
                    values.put("time", dateTime);
                    values.put("latitude", latitude);
                    values.put("longitude", longitude);
                    values.put("data", ozoneLayerData.getData());
                    values.put("lastUpdateTime", Calendar.getInstance().getTimeInMillis());

                    getContentResolver().insert(OpenWeatherMapContentProvider.CONTENT_URI, values);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
                catch (RemoteException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        };
        thread.start();
    }

    public class OzoneLayerDataServiceBinder extends Binder
    {
        OzoneLayerDataService getService()
        {
            return OzoneLayerDataService.this;
        }
    }
}
