package com.wearcast.app.ui.fragments.settings.location

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.wearcast.app.Constants
import com.wearcast.app.R
import com.wearcast.app.data.model.City

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchResultItemViewHolder>(){

    var searchResults = emptyList<City>()

    private lateinit var context: Context

    inner class SearchResultItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSearchResultItemName: TextView = itemView.findViewById(R.id.tvSearchResultItemName)
        val tvSearchResultItemCountry: TextView = itemView.findViewById(R.id.tvSearchResultItemCountry)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultItemViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_result, parent, false)
        return SearchResultItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: SearchResultItemViewHolder, position: Int) {

        val currentItem = searchResults[position]

        holder.apply {
            tvSearchResultItemName.text = currentItem.name
            tvSearchResultItemCountry.text = currentItem.country

            itemView.setOnClickListener {
                val sharedPref = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                val edit = sharedPref.edit()
                edit.putString(Constants.SELECTED_CITY_NAME, currentItem.name)
                edit.putString(Constants.SELECTED_CITY_LATITUDE, currentItem.lat.toString())
                edit.putString(Constants.SELECTED_CITY_LONGITUDE, currentItem.lon.toString())
                edit.commit()
                Toast.makeText(context, context.resources.getString(R.string.settings_location_location_confirmation, currentItem.name, currentItem.country), Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun getItemCount(): Int {
        return searchResults.size
    }

    fun setData(searchResults: List<City>) {
        this.searchResults = searchResults
        notifyDataSetChanged()
    }
}