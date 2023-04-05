package com.wearcast.app.ui.fragments.settings.location

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.wearcast.app.*
import com.wearcast.app.databinding.FragmentSettingsLocationBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import android.view.MotionEvent

import android.view.View.OnTouchListener
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import com.wearcast.app.data.model.City


class SettingsLocationFragment : Fragment() {

    private var _binding: FragmentSettingsLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var requestPermission: ActivityResultLauncher<String>
    private lateinit var sharedPref: SharedPreferences
    private lateinit var editSharedPref: SharedPreferences.Editor
    private lateinit var repo: WeatherRepositorySafe
    private lateinit var searchAdapter: SearchAdapter
    private val TAG = "SettingsFragment"
    private var searchButtonLastClickTime : Long = 0

    private var currentLocation: Location? = null
    lateinit var locationManager: LocationManager

    private var isSearchOn = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsLocationBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val appContainer = (requireActivity().application as MyApplication).appContainer
        repo = appContainer.weatherRepositorySafe
        sharedPref = appContainer.sharedPref
        editSharedPref = sharedPref.edit()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ivLocationBack.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.changeFragment(SettingsFragment())
        }

        initializeRequestPermission()

        setUpStateBasedOnAutoLocation()

        setUpSearchBarListeners()

        setUpSearchRecyclerView()
    }

    private fun initializeRequestPermission() {

        requestPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                getDeviceLocation()
            }
            else {
                binding.swAutoLocation.isChecked = false
                binding.etSearchBar.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.VISIBLE
                Toast.makeText(
                    context,
                    "Please allow permission in dialog or in phone settings",
                    Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    //permission handling is done in initializeRequestPermission and setUpStateBasedOnAutoLocation
    @SuppressLint("MissingPermission")
    fun getDeviceLocation() {

        locationManager = (requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager)

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

        currentLocation?.let { setAppLocation(it) }
    }

    private fun setAppLocation(location: Location) {

        if((activity as MainActivity).isNetworkConnected()) {

            lifecycleScope.launch(Dispatchers.IO) {

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

                        Toast.makeText(context, resources.getString(R.string.settings_location_location_confirmation, currentWeatherResponse.data.name, currentWeatherResponse.data.sys.country), Toast.LENGTH_SHORT).show()

                    }

                }
                else if (currentWeatherResponse is Resource.Error) {
                    Log.e(TAG, "An error occured: ${currentWeatherResponse.message}")
                }
            }
        }
    }

    private fun setUpStateBasedOnAutoLocation(){

        if(ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            editSharedPref.putBoolean(Constants.SETTINGS_AUTO_SELECTION_SWITCH_STATE, false)
            editSharedPref.commit()
        }

        val isLocationAuto = sharedPref.getBoolean(Constants.SETTINGS_AUTO_SELECTION_SWITCH_STATE, false)
        binding.swAutoLocation.isChecked = isLocationAuto

        if (isLocationAuto) binding.etSearchBar.visibility = View.GONE else binding.etSearchBar.visibility = View.VISIBLE
        if (isLocationAuto) binding.rvSearchResults.visibility = View.GONE else binding.rvSearchResults.visibility = View.VISIBLE

        binding.swAutoLocation.setOnCheckedChangeListener {
                _,
                isChecked ->
            if(isChecked){
                requestPermission.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
                binding.etSearchBar.visibility = View.GONE
                binding.rvSearchResults.visibility = View.GONE
                editSharedPref.putBoolean(Constants.SETTINGS_AUTO_SELECTION_SWITCH_STATE, true)
                editSharedPref.commit()
            }

            else {
                binding.etSearchBar.visibility = View.VISIBLE
                binding.rvSearchResults.visibility = View.VISIBLE
                editSharedPref.putBoolean(Constants.SETTINGS_AUTO_SELECTION_SWITCH_STATE, false)
                editSharedPref.commit()
            }
        }
    }

    private suspend fun setSearchAdapterDataSafe(adapter: SearchAdapter) {

        try {
            if((activity as MainActivity).isNetworkConnected()) {
                val fetchedCitiesListResponse = repo.getCitiesFromStringSafe(
                    binding.etSearchBar.text.toString(),
                    Constants.LIMIT,
                    BuildConfig.API_KEY
                )
                if (fetchedCitiesListResponse is Resource.Success && fetchedCitiesListResponse.data != null) {
                    withContext(Dispatchers.Main) {
                        adapter.setData(fetchedCitiesListResponse.data)
                    }
                }
                else if(fetchedCitiesListResponse is Resource.Error) {
                    Log.e(TAG, "An error occured: ${fetchedCitiesListResponse.message}")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "An error occured: ${fetchedCitiesListResponse.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e(TAG, "An error occured: No internet connection")
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "An error occured: No internet connection", Toast.LENGTH_SHORT).show()
                }
            }
        } catch(t: Throwable) {
            when(t) {
                is IOException ->  {
                    Log.e(TAG, "An error occured: Network Failure")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "An error occured: Network Failure", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    Log.e(TAG, "An error occured: Conversion error")
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "An error occured: Conversion error", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpSearchBarListeners() {
        binding.etSearchBar.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_LEFT = 0
            if (event.action == MotionEvent.ACTION_UP) {

                if(event.getX() <= (binding.etSearchBar.getCompoundDrawables()[DRAWABLE_LEFT].getBounds().width())) {

                    if(isSearchOn) {
                        //turn off
                        isSearchOn = false
                        animateSearchBarDown()
                        binding.etSearchBar.text.clear()
                        binding.etSearchBar.clearFocus()
                        searchAdapter.setData(emptyList<City>())
                        hideKeyboard()
                        return@OnTouchListener true
                    }
                }

                //animate only if etSearchBar doesn't have focus (i. e. only if other views aren't already hidden)
                if(!isSearchOn) {
                    animateSearchBarUp()
                }
                //turn on
                isSearchOn = true
            }
            false
        })

        binding.etSearchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard()
                //prevent rapid clicking
                if (SystemClock.elapsedRealtime() - searchButtonLastClickTime < 3000){
                    return@setOnEditorActionListener false
                }
                searchButtonLastClickTime = SystemClock.elapsedRealtime()

                lifecycleScope.launch(Dispatchers.IO) {
                    setSearchAdapterDataSafe(searchAdapter)
                }
            }
            false
        }
    }
    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun animateSearchBarUp(){
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.duration = 300
        binding.ivLocationBack.visibility = View.GONE
        binding.tvLocationLabel.visibility = View.GONE
        binding.tvSetAuto.visibility = View.GONE
        binding.swAutoLocation.visibility = View.GONE
        binding.ivLocationBack.startAnimation(fadeOut)
        binding.tvLocationLabel.startAnimation(fadeOut)
        binding.swAutoLocation.startAnimation(fadeOut)
        binding.tvSetAuto.startAnimation(fadeOut)

        val jumpUp = TranslateAnimation(0F, 0F, 300F, 0F)
        jumpUp.duration = 500
        binding.etSearchBar.startAnimation(jumpUp)
        binding.etSearchBar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_back, 0, 0, 0)
    }

    private fun animateSearchBarDown(){
        val jumpDown = TranslateAnimation(0F, 0F, -300F, 0F)
        jumpDown.duration = 500
        binding.etSearchBar.startAnimation(jumpDown)
        binding.etSearchBar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search, 0, 0, 0)

        val fadeIn = AlphaAnimation(0f, 1f)
        fadeIn.interpolator = DecelerateInterpolator()
        fadeIn.duration = 1000
        binding.ivLocationBack.visibility = View.VISIBLE
        binding.tvLocationLabel.visibility = View.VISIBLE
        binding.tvSetAuto.visibility = View.VISIBLE
        binding.swAutoLocation.visibility = View.VISIBLE
        binding.ivLocationBack.startAnimation(fadeIn)
        binding.tvLocationLabel.startAnimation(fadeIn)
        binding.swAutoLocation.startAnimation(fadeIn)
        binding.tvSetAuto.startAnimation(fadeIn)
    }

    private fun setUpSearchRecyclerView() {
        searchAdapter = SearchAdapter()
        binding.rvSearchResults.adapter = searchAdapter
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_grey)?.let { divider.setDrawable(it) }
        binding.rvSearchResults.addItemDecoration(divider)
    }
}