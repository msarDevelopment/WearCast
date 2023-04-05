package com.wearcast.app

import android.content.Context
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wearcast.app.databinding.ActivityMainBinding
import com.wearcast.app.ui.fragments.whattowear.WhatToWearFragment
import com.google.android.material.tabs.TabLayoutMediator
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var pagerAdapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //for transparent statusbar
        with(window) {
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }

        pagerAdapter = PagerAdapter(this)
        binding.vpMainActivity.adapter = pagerAdapter

        TabLayoutMediator(binding.tlDotSelector, binding.vpMainActivity) {tab, position -> ""}.attach()
    }

    private class PagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 2

        var fragments = ArrayList<Fragment>()

        init {
            fragments.add(WhatToWearFragment())
            fragments.add(SettingsFragment())
        }

        override fun createFragment(position: Int): Fragment {
            return if(position == 0)
                fragments[0]
            else
                fragments[1]
        }

        override fun getItemId(position: Int): Long {
            val fragment: Fragment = fragments.get(position)
            return getIDForFragment(fragment)
        }

        override fun containsItem(itemId: Long): Boolean {
            for (fragment in fragments) {
                if (getIDForFragment(fragment) == itemId) return true
            }
            return false
        }

        private fun getIDForFragment(fragment: Fragment): Long {
            return fragment.hashCode().toLong()
        }

        fun replaceFragment(index: Int, fragment: Fragment?) {
            fragments[index] = fragment!!
            notifyDataSetChanged()
        }

    }

    fun isNetworkConnected(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun attachBaseContext(newBase: Context?) {

        if(newBase != null) {
            val sp = PreferenceManager.getDefaultSharedPreferences(newBase)
            val language = sp.getString("app_language", "en")

            val locale = when(language) {
                "en" -> { Locale("en") }
                "hr" -> { Locale("hr") }
                else -> {
                    if (Build.VERSION.SDK_INT >= 24) {
                        Resources.getSystem().configuration.locales.get(0);
                    }
                    else {
                        Resources.getSystem().configuration.locale
                    }
                }
            }

            Locale.setDefault(locale)
            newBase.resources.configuration.setLocale(locale)
            applyOverrideConfiguration(newBase.resources.configuration)
        }

        super.attachBaseContext(newBase)
    }

    fun changeFragment(fragment: Fragment) {
        pagerAdapter.replaceFragment(1, fragment)
    }
}