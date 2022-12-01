package com.wearcast.app

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.wearcast.app.databinding.FragmentSettingsBinding
import com.wearcast.app.ui.fragments.settings.SettingsOptionsAdapter


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var settingsOptionsAdapter: SettingsOptionsAdapter
    private val TAG = "SettingsFragment"

    @SuppressLint("ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val settingsList = listOf<String>(
            getString(R.string.settings_location_label),
            getString(R.string.settings_language_label),
            getString(R.string.settings_temperatures_label),
        )

        settingsOptionsAdapter = SettingsOptionsAdapter(settingsList, {Fragment -> changeFragment(Fragment)})
        binding.rvSettingsOptions.adapter = settingsOptionsAdapter
        binding.rvSettingsOptions.layoutManager = LinearLayoutManager(requireContext())
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_grey)?.let { divider.setDrawable(it) }
        binding.rvSettingsOptions.addItemDecoration(divider)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun changeFragment(fragment: Fragment) {
        val activity = requireActivity() as MainActivity
        activity.changeFragment(fragment)
    }
}

data class Language(
    var name: String,
    var short: String
)