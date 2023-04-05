package com.wearcast.app.ui.fragments.settings.temperatures

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wearcast.app.Constants
import com.wearcast.app.MainActivity
import com.wearcast.app.MyApplication
import com.wearcast.app.SettingsFragment
import com.wearcast.app.databinding.FragmentSettingsTemperaturesBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

class SettingsTemperaturesFragment : Fragment() {

    private var _binding: FragmentSettingsTemperaturesBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPref: SharedPreferences
    private lateinit var editSharedPref: SharedPreferences.Editor

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsTemperaturesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val appContainer = (requireActivity().application as MyApplication).appContainer
        sharedPref = appContainer.sharedPref
        editSharedPref = sharedPref.edit()

        setUpTemperaturePickers()

        BottomSheetBehavior.from(binding.bottomSheet).apply {
            peekHeight = 100
            this.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        return root
    }

    private fun setUpTemperaturePickers() {

        binding.ivTemperaturesBack.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.changeFragment(SettingsFragment())
        }

        val temperatureCold = sharedPref.getInt(Constants.TEMPERATURE_COLD, 10)
        val temperatureChilly = sharedPref.getInt(Constants.TEMPERATURE_CHILLY, 15)
        val temperatureWarm = sharedPref.getInt(Constants.TEMPERATURE_WARM, 20)

        binding.npCold.minValue = 0
        binding.npCold.maxValue = temperatureChilly - 1
        binding.npCold.value = temperatureCold
        binding.npCold.wrapSelectorWheel = false

        binding.npCold.setOnValueChangedListener { picker, oldVal, newVal ->
            binding.npChilly.minValue = newVal + 1
            editSharedPref.putInt(Constants.TEMPERATURE_COLD, newVal)
            editSharedPref.commit()
        }

        binding.npChilly.minValue = temperatureCold + 1
        binding.npChilly.maxValue = temperatureWarm
        binding.npChilly.value = temperatureChilly
        binding.npChilly.wrapSelectorWheel = false

        binding.npChilly.setOnValueChangedListener { picker, oldVal, newVal ->
            binding.npCold.maxValue = newVal - 1
            binding.npWarm.minValue = newVal + 1
            editSharedPref.putInt(Constants.TEMPERATURE_CHILLY, newVal)
            editSharedPref.commit()
        }

        binding.npWarm.minValue = temperatureChilly + 1
        binding.npWarm.maxValue = Constants.HARD_HOT_LIMIT
        binding.npWarm.value = temperatureWarm
        binding.npWarm.wrapSelectorWheel = false

        binding.npWarm.setOnValueChangedListener { picker, oldVal, newVal ->
            binding.npChilly.maxValue = newVal - 1
            editSharedPref.putInt(Constants.TEMPERATURE_WARM, newVal)
            editSharedPref.commit()
        }
    }
}