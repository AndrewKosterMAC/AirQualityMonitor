package com.webfactional.andrewk.airqualitymonitor;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IOpenWeatherMapApi
{
    @GET("pollution/v1/o3/{location}/{datetime}.json")
    Call<OzoneLayerData> getOzoneLayerData(
        @Path("location") String location,
        @Path("datetime") String dateTime,
        @Query("appid") String apiKey);
}