package com.wearcast.app.ui.fragments.settings.languages

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.wearcast.app.Language
import com.wearcast.app.MainActivity
import com.wearcast.app.R
import com.wearcast.app.SettingsFragment
import com.wearcast.app.databinding.FragmentSettingsLanguagesBinding

class SettingsLanguagesFragment : Fragment() {

    private var _binding: FragmentSettingsLanguagesBinding? = null
    private val binding get() = _binding!!
    private lateinit var languageAdapter: LanguageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsLanguagesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setUpLanguageRecyclerView()

        return root
    }

    private fun setUpLanguageRecyclerView(){

        binding.ivLanguagesBack.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.changeFragment(SettingsFragment())
        }

        val eng = Language(getString(R.string.settings_language_english_label), "en")
        val cro = Language(getString(R.string.settings_language_croatian_label), "hr")
        val languages: List<Language> = listOf(eng, cro)

        languageAdapter = LanguageAdapter()
        binding.rvLanguageList.adapter = languageAdapter
        binding.rvLanguageList.layoutManager = LinearLayoutManager(requireContext())
        val divider = DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.divider_grey)?.let { divider.setDrawable(it) }
        binding.rvLanguageList.addItemDecoration(divider)
        languageAdapter.setData(languages)

    }
}