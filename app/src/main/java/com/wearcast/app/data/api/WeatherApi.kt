package com.wearcast.app.data.api

import com.wearcast.app.data.model.City
import com.wearcast.app.data.model.weathercurrent.WeatherCurrent
import com.wearcast.app.data.model.weatherforecast.WeatherForecast
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {

    @GET("/data/2.5/weather")
    suspend fun getCurrentWeatherFromString(
        @Query("q") q: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Response<WeatherCurrent>

    @GET("/data/2.5/weather")
    suspend fun getCurrentWeatherFromCoordinates(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Response<WeatherCurrent>

    @GET("/data/2.5/forecast")
    suspend fun getForecastFromString(
        @Query("q") q: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Response<WeatherForecast>

    @GET("/data/2.5/forecast")
    suspend fun getForecastFromCoordinates(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Response<WeatherForecast>

    @GET("/geo/1.0/direct")
    suspend fun getCitiesFromString(
        @Query("q") q: String,
        @Query("limit") limit: String,
        @Query("appid") appid: String
    ): Response<List<City>>

    @GET("/geo/1.0/reverse")
    suspend fun getCitiesFromCoordinates(
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("appid") appid: String
    ): Response<List<City>>

}