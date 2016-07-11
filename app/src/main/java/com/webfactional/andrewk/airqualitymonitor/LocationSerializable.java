package com.webfactional.andrewk.airqualitymonitor;

import android.location.Location;

import java.io.Serializable;

public class LocationSerializable implements Serializable
{
    public double getLongitude()
    {
        return longitude;
    }

    private double longitude;

    public void setLongitude(double value)
    {
        longitude = value;
    }

    public double getLatitude()
    {
        return latitude;
    }

    private double latitude;

    public void setLatitude(double value)
    {
        latitude = value;
    }

    @Override
    public String toString()
    {
        return "Location{latitude: " + getLatitude() + ", longitude: " + getLongitude() + "}";
    }

    public LocationSerializable()
    {
        latitude = -1;
        longitude = -1;
    }

    public LocationSerializable(Location location)
    {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }
}
