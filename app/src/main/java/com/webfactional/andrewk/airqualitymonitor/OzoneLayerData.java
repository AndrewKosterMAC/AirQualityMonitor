package com.webfactional.andrewk.airqualitymonitor;

import java.io.Serializable;

/**
 *
 */
public class OzoneLayerData implements Serializable
{
    public String getTime()
    {
        return time;
    }

    private String time;

    public void setTime(String value)
    {
        time = value;
    }

    public LocationSerializable getLocation()
    {
        return location;
    }

    private LocationSerializable location;

    public void setLocation(LocationSerializable value)
    {
        location = value;
    }

    public double getData()
    {
        return data;
    }

    private double data;

    public void setData(double value)
    {
        data = value;
    }

    @Override
    public String toString()
    {
        return "OzoneLayerData{time: " + getTime() + ", location: " + getLocation() + ", data: " + getData() + "}";
    }
}
