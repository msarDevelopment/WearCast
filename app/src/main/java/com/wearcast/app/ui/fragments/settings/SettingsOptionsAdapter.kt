package com.wearcast.app.ui.fragments.settings

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.wearcast.app.R
import com.wearcast.app.ui.fragments.settings.languages.SettingsLanguagesFragment
import com.wearcast.app.ui.fragments.settings.location.SettingsLocationFragment
import com.wearcast.app.ui.fragments.settings.temperatures.SettingsTemperaturesFragment

class SettingsOptionsAdapter(
    var settingsOptions: List<String>,
    val openFragment: (Fragment) -> Unit
) : RecyclerView.Adapter<SettingsOptionsAdapter.SettingsOptionsItemViewHolder>(){

    private lateinit var typeface: Typeface
    private lateinit var context: Context

    inner class SettingsOptionsItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOptionName: TextView = itemView.findViewById(R.id.tvItemName)
        val ivArrow: ImageView = itemView.findViewById(R.id.ivItemIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsOptionsItemViewHolder {
        context = parent.context
        typeface = ResourcesCompat.getFont(context, R.font.poppins_semibold)!!
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_name_and_icon, parent, false)
        return SettingsOptionsItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SettingsOptionsItemViewHolder, position: Int) {

        val currentItem = settingsOptions[position]

        holder.apply {
            tvOptionName.text = currentItem
            tvOptionName.typeface = typeface
            ivArrow.setImageResource(R.drawable.ic_arrow)
            ivArrow.visibility = View.VISIBLE

            itemView.setOnClickListener {
                when(position) {
                    0 -> openFragment(SettingsLocationFragment())
                    1 -> openFragment(SettingsLanguagesFragment())
                    2 -> openFragment(SettingsTemperaturesFragment())
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return settingsOptions.size
    }
}