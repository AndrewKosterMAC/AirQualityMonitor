package com.webfactional.andrewk.airqualitymonitor;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity
{
    LocationService locationService;

    GoogleApiClient googleApiClientForLocationService;

    public GoogleApiClient getGoogleApiClientForLocationService()
    {
        return googleApiClientForLocationService;
    }

    private String lastLatitude = "";

    private String lastLongitude = "";

    private TextView dataDisplay;

    private OzoneLayerDataService ozoneLayerDataService;

    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            OzoneLayerDataService.OzoneLayerDataServiceBinder binder = (OzoneLayerDataService.OzoneLayerDataServiceBinder)iBinder;
            ozoneLayerDataService = binder.getService();

            isServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            isServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataDisplay = (TextView)findViewById(R.id.dataDisplay);

        locationService = new LocationService(this);

        googleApiClientForLocationService = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(locationService)
                .addOnConnectionFailedListener(locationService)
                .build();
        googleApiClientForLocationService.connect();
    }

    public void onLocationObtained(Location obtainedLocation)
    {
        String latitude = String.format("%.1f", obtainedLocation.getLatitude());
        String longitude = String.format("%.1f", obtainedLocation.getLongitude());

        if (!latitude.equals(lastLatitude) || !longitude.equals(lastLongitude))
        {
            lastLatitude = latitude;
            lastLongitude = longitude;

            Intent intent = new Intent(this, OzoneLayerDataService.class);
            intent.putExtra("latitude", latitude);
            intent.putExtra("longitude", longitude);
            intent.putExtra("ozoneLayerDataMessenger", new Messenger(ozoneLayerDataMessageHandler));
            intent.putExtra("isNetworkAvailable", isNetworkAvailable(((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo()));

            startService(intent);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unbindService(serviceConnection);
    }

    public boolean isNetworkAvailable(NetworkInfo networkInfo)
    {
        return networkInfo != null && networkInfo.isAvailable();
    }

    Handler ozoneLayerDataMessageHandler = new Handler()
    {
        @Override
        public void handleMessage(Message message)
        {
            OzoneLayerData ozoneLayerData = (OzoneLayerData)message.obj;

            dataDisplay.setText(ozoneLayerData.toString());
        }
    };
}
