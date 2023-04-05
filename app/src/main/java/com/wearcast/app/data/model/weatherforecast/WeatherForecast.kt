package com.wearcast.app.data.model.weatherforecast


import com.google.gson.annotations.SerializedName

data class WeatherForecast(
    @SerializedName("cod")
    val cod: String,
    @SerializedName("message")
    val message: Int,
    @SerializedName("cnt")
    val cnt: Int,
    @SerializedName("list")
    val list: List<Forecast>,
    @SerializedName("city")
    val city: City
)