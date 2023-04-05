package com.wearcast.app.ui.fragments.whattowear

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.PlaybackParams
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.wearcast.app.*
import com.wearcast.app.data.model.weathercurrent.WeatherCurrent
import com.wearcast.app.data.model.weatherforecast.Forecast
import com.wearcast.app.databinding.FragmentWhatToWearBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.round


class WhatToWearFragment : Fragment() {

    private var _binding: FragmentWhatToWearBinding? = null
    private val binding get() = _binding!!

    private lateinit var repo: WeatherRepositorySafe
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editSharedPref: SharedPreferences.Editor

    private lateinit var adapter: ForecastAdapter

    private val TAG = "WhatToWearFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentWhatToWearBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val appContainer = (requireActivity().application as MyApplication).appContainer
        repo = appContainer.weatherRepositorySafe
        sharedPref = appContainer.sharedPref
        editSharedPref = sharedPref.edit()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapter()

        binding.swipeToRefresh.setOnRefreshListener {

            lifecycleScope.launch(Dispatchers.IO){
                Log.e(TAG, "starting pull to refresh in ${Thread.currentThread().name}")
                loadDataOrShowError()
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "ending pull to refresh in ${Thread.currentThread().name}")
                    binding.swipeToRefresh.isRefreshing = false
                }

            }

        }
    }

    override fun onPause() {
        super.onPause()
        binding.vvBackground.suspend()
    }

    override fun onResume() {
        super.onResume()
        binding.vvBackground.resume()
        lifecycleScope.launch(Dispatchers.IO) {
            Log.e(TAG, "onResume in ${Thread.currentThread().name}")
            checkIsLocationAutoAndUpdate()
            loadDataOrShowError()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.vvBackground.stopPlayback()
    }

    private fun setAdapter() {
        adapter = ForecastAdapter()
        binding.rvForecast.adapter = adapter
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        binding.rvForecast.layoutManager = layoutManager
    }

    private suspend fun loadDataOrShowError() {

        if((activity as MainActivity).isNetworkConnected()) {
            Log.e(TAG, "loadDataOrShowError in ${Thread.currentThread().name}")
            withContext(Dispatchers.Main) {
                showViews(true)
            }
            initializeData()
        }
        else{
            Log.e(TAG, "loadDataOrShowError in ${Thread.currentThread().name}")
            withContext(Dispatchers.Main) {
                showViews(false)
            }
        }
    }

    private fun showViews(show: Boolean) {
        if(show) {
            binding.allViews.visibility = View.VISIBLE
            binding.noInternetViews.visibility = View.INVISIBLE
        }
        else {
            binding.allViews.visibility = View.INVISIBLE
            binding.head.visibility = View.INVISIBLE
            binding.accessory.visibility = View.INVISIBLE
            binding.noInternetViews.visibility = View.VISIBLE
        }
    }

    private suspend fun initializeData() {

        Log.e(TAG, "initializeData in ${Thread.currentThread().name}")

        val location = sharedPref.getString(Constants.SELECTED_CITY_NAME, "San Francisco")
        val lat = sharedPref.getString(Constants.SELECTED_CITY_LATITUDE, "37.7790262")
        val lon = sharedPref.getString(Constants.SELECTED_CITY_LONGITUDE, "-122.419906")
        val currentWeatherResponse = repo.getCurrentWeatherFromCoordinatesSafe(lat!!, lon!!,
            BuildConfig.API_KEY,
            Constants.UNITS_METRIC
        )

        if (currentWeatherResponse is Resource.Success && currentWeatherResponse.data != null) {

            val currentWeather = currentWeatherResponse.data

            withContext(Dispatchers.Main) {

                Log.e(TAG, "updating UI in ${Thread.currentThread().name}")

                val icon = currentWeather.weather[0].icon
                setBackgroundVideo(icon)
                binding.weatherIcon.load("https://openweathermap.org/img/wn/$icon@2x.png")

                binding.locationName.text = location
                binding.temperature.text = resources.getString(R.string.ei_temp, String.format("%.0f", round(avoidNegativeZero(currentWeather.main.temp))))
                binding.feelsLike.text = resources.getString(R.string.ei_feelslike, String.format("%.0f", round(avoidNegativeZero(currentWeather.main.feelsLike))))

                setOutfit(currentWeather)

                binding.temperature.text = resources.getString(R.string.ei_temp, String.format("%.0f", round(currentWeather.main.temp)))

                binding.aiHumidity.setValue(resources.getString(R.string.ai_humidity_value, currentWeather.main.humidity.toString()))
                binding.aiWind.setValue(resources.getString(R.string.ai_wind_value, String.format("%.0f", round(currentWeather.wind.speed))))
                binding.aiPressure.setValue(resources.getString(R.string.ai_pressure_value, currentWeather.main.pressure.toString()))

            }

        }
        else if (currentWeatherResponse is Resource.Error) {
            Log.e(TAG, "An error occured: ${currentWeatherResponse.message}")
        }

        val wholeForecastObjectResponse = repo.getWholeForecastObjectFromCoordinatesSafe(lat, lon,
            BuildConfig.API_KEY,
            Constants.UNITS_METRIC
        )

        if (wholeForecastObjectResponse is Resource.Success && wholeForecastObjectResponse.data != null) {
            val wholeForecastObject = wholeForecastObjectResponse.data
            withContext(Dispatchers.Main) {
                adapter.setData(wholeForecastObject.list.take(8) as List<Forecast>, wholeForecastObject.city.timezone)
                //Toast.makeText(requireContext(), "called", Toast.LENGTH_SHORT).show()
            }
        }
        else if (wholeForecastObjectResponse is Resource.Error) {
            Log.e(TAG, "An error occured: ${wholeForecastObjectResponse.message}")
        }

    }

    private fun avoidNegativeZero(temp: Double): Double {
        var roundTemp = round(temp)
        roundTemp = if (roundTemp == -0.0) 0.0 else roundTemp
        return roundTemp
    }

    private fun setOutfit(currentWeather: WeatherCurrent) {

        val temperatureCold = sharedPref.getInt(Constants.TEMPERATURE_COLD, 10)
        val temperatureChilly = sharedPref.getInt(Constants.TEMPERATURE_CHILLY, 15)
        val temperatureWarm = sharedPref.getInt(Constants.TEMPERATURE_WARM, 20)

        if (currentWeather.rain != null) {
            binding.accessory.visibility = View.VISIBLE
            binding.accessory.text = resources.getString(R.string.wtw_accessory)
        }
        else {
            binding.accessory.visibility = View.GONE
            binding.accessory.text = ""
        }

        if (currentWeather.main.temp <= temperatureCold) {
            binding.head.visibility = View.VISIBLE
            binding.head.text = resources.getString(R.string.wtw_head)
        }
        else {
            binding.head.visibility = View.GONE
            binding.head.text = ""
        }

        when {
            currentWeather.main.temp <= temperatureCold -> {
                binding.torso.text = resources.getString(R.string.wtw_torso_cold)
                binding.legs.text = resources.getString(R.string.wtw_legs_cold)
                binding.feet.text = resources.getString(R.string.wtw_feet_cold)
            }
            currentWeather.main.temp > temperatureCold && currentWeather.main.temp <= temperatureChilly -> {
                binding.torso.text = resources.getString(R.string.wtw_torso_chilly)
                binding.legs.text = resources.getString(R.string.wtw_legs_chilly_mild)
                binding.feet.text = resources.getString(R.string.wtw_feet_chilly_mild_hot)
            }
            currentWeather.main.temp > temperatureChilly && currentWeather.main.temp <= temperatureWarm -> {
                binding.torso.text = resources.getString(R.string.wtw_torso_mild)
                binding.legs.text = resources.getString(R.string.wtw_legs_chilly_mild)
                binding.feet.text = resources.getString(R.string.wtw_feet_chilly_mild_hot)
            }
            else -> {
                binding.torso.text = resources.getString(R.string.wtw_torso_hot)
                binding.legs.text = resources.getString(R.string.wtw_legs_hot)
                binding.feet.text = resources.getString(R.string.wtw_feet_chilly_mild_hot)
            }
        }
    }

    private fun setBackgroundVideo(icon: String) {

        //no need for else in when because it will default to this
        var video = R.raw.day_clear

        when (icon) {
            "03d", "04d" -> video = R.raw.day_clouds
            "09d", "10d", "11d" -> video = R.raw.day_rain
            "13d"-> video = R.raw.day_snow
            "50d" -> video = R.raw.day_fog
            "01n", "02n" -> video = R.raw.night_clear
            "03n", "04n" -> video = R.raw.night_clouds
            "09n", "10n", "11n" -> video = R.raw.night_rain
            "13n"-> video = R.raw.night_snow
            "50n" -> video = R.raw.night_fog
        }

        val uri = Uri.parse("android.resource://${activity?.packageName}/$video")

        binding.vvBackground.setVideoURI(uri)
        binding.vvBackground.start()

        binding.vvBackground.setOnPreparedListener {

            val playbackParams = PlaybackParams()
            playbackParams.speed = 0.5f
            it.playbackParams = playbackParams

            it.isLooping = true
        }
    }

    private suspend fun checkIsLocationAutoAndUpdate(){

        val isLocationAuto = sharedPref.getBoolean(Constants.SETTINGS_AUTO_SELECTION_SWITCH_STATE, false)

        if(isLocationAuto) {
            if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation()
            }
        }
    }

    //permission handling is done in checkIsLocationAutoAndUpdate
    @SuppressLint("MissingPermission")
    suspend fun getDeviceLocation() {

        var currentLocation: Location? = null

        withContext(Dispatchers.Main) {

            val locationManager: LocationManager = (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager)

            val hasNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            val networkLocationListener: LocationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    currentLocation = location
                }
                override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            if (hasNetwork) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    5000,
                    0F,
                    networkLocationListener
                )
            }

            val lastKnownLocationByNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            lastKnownLocationByNetwork?.let { currentLocation = lastKnownLocationByNetwork }
        }

        currentLocation?.let { setAppLocation(it) }
    }


    suspend fun setAppLocation(location: Location) {

        if((activity as MainActivity).isNetworkConnected()) {

            val currentWeatherResponse = repo.getCurrentWeatherFromCoordinatesSafe(
                location.latitude.toString(),
                location.longitude.toString(),
                BuildConfig.API_KEY,
                Constants.UNITS_METRIC
            )

            if (currentWeatherResponse is Resource.Success && currentWeatherResponse.data != null) {

                withContext(Dispatchers.Main) {
                    editSharedPref.putString(Constants.SELECTED_CITY_NAME, currentWeatherResponse.data.name)
                    editSharedPref.putString(Constants.SELECTED_CITY_LATITUDE, location.latitude.toString())
                    editSharedPref.putString(Constants.SELECTED_CITY_LONGITUDE, location.longitude.toString())
                    editSharedPref.commit()
                }

            }
            else if (currentWeatherResponse is Resource.Error) {
                Log.e(TAG, "An error occured: ${currentWeatherResponse.message}")
            }
        }
    }
}