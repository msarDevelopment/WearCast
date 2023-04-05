package com.wearcast.app

import android.content.Context
import android.content.SharedPreferences
import com.wearcast.app.data.api.Retrofit

class AppContainer(context: Context) {
    private val retrofit = Retrofit()
    private val api = retrofit.api
    val weatherRepositorySafe = WeatherRepositorySafe(api)
    val sharedPref: SharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
}