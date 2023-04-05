package com.wearcast.app

import com.wearcast.app.data.api.WeatherApi
import com.wearcast.app.data.model.City
import com.wearcast.app.data.model.weathercurrent.WeatherCurrent
import com.wearcast.app.data.model.weatherforecast.Forecast
import com.wearcast.app.data.model.weatherforecast.WeatherForecast
import retrofit2.Response

class WeatherRepositorySafe(val api: WeatherApi) {

    suspend fun getCurrentWeatherFromStringSafe(
        q: String,
        appid: String,
        units: String
    ): Resource<WeatherCurrent> {
        val response: Response<WeatherCurrent> =
            api.getCurrentWeatherFromString(q, appid, units)
        return if(response.isSuccessful && response.body() != null) {
            Resource.Success(response.body()!!)
        } else {
            Resource.Error(response.message())
        }
    }

    suspend fun getCurrentWeatherFromCoordinatesSafe(
        lat: String,
        lon: String,
        appid: String,
        units: String
    ): Resource<WeatherCurrent> {

        val response: Response<WeatherCurrent> =
            api.getCurrentWeatherFromCoordinates(lat, lon, appid, units)

        return if(response.isSuccessful && response.body() != null) {
            Resource.Success(response.body()!!)
        } else {
            Resource.Error(response.message())
        }
    }

    suspend fun getForecastFromString(
        q: String,
        appid: String,
        units: String
    ): List<Forecast?>? {

        val response: Response<WeatherForecast> =
            api.getForecastFromString(q, appid, units)

        var fetchedCurrentWeather: WeatherForecast? = null
        var fetchedForecast: List<Forecast?>? = null

        if (response.isSuccessful && response.body() != null) {
            fetchedCurrentWeather = response.body()
            if (fetchedCurrentWeather != null) {
                fetchedForecast = fetchedCurrentWeather.list
            }
            return fetchedForecast
        }

        return fetchedForecast
    }

    suspend fun getForecastFromCoordinates(
        lat: String,
        lon: String,
        appid: String,
        units: String
    ): List<Forecast?>? {

        val response: Response<WeatherForecast> =
            api.getForecastFromCoordinates(lat, lon, appid, units)

        var fetchedWeatherForecast: WeatherForecast? = null
        var fetchedForecasts: List<Forecast?>? = null

        if (response.isSuccessful && response.body() != null) {
            fetchedWeatherForecast = response.body()
            if (fetchedWeatherForecast != null) {
                fetchedForecasts = fetchedWeatherForecast.list
            }
            return fetchedForecasts
        }

        return fetchedForecasts
    }

    suspend fun getWholeForecastObjectFromCoordinatesSafe(
        lat: String,
        lon: String,
        appid: String,
        units: String
    ): Resource<WeatherForecast> {

        val response: Response<WeatherForecast> =
            api.getForecastFromCoordinates(lat, lon, appid, units)

        return if(response.isSuccessful && response.body() != null) {
            Resource.Success(response.body()!!)
        } else {
            Resource.Error(response.message())
        }
    }

    suspend fun getCitiesFromCoordinatesSafe(
        lat: String,
        lon: String,
        appid: String
    ): Resource<List<City>> {

        val response: Response<List<City>> =
            api.getCitiesFromCoordinates(lat, lon, appid)

        return if(response.isSuccessful && response.body() != null) {
            Resource.Success(response.body()!!)
        } else {
            Resource.Error(response.message())
        }
    }

    suspend fun getCitiesFromStringSafe(
        q: String,
        limit: String,
        appid: String
    ): Resource<List<City>> {

        val response: Response<List<City>> =
            api.getCitiesFromString(q, limit, appid)

        return if(response.isSuccessful && response.body() != null) {
            Resource.Success(response.body()!!)
        } else {
            Resource.Error(response.message())
        }
    }
}