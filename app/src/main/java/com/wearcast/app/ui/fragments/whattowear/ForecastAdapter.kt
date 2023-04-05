package com.wearcast.app.ui.fragments.whattowear

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.wearcast.app.R
import com.wearcast.app.data.model.weatherforecast.Forecast
import java.text.SimpleDateFormat
import kotlin.math.round

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.WeatherForecastViewHolder>(){

    var threeHourForecasts = emptyList<Forecast>()
    var timezone = 0

    inner class WeatherForecastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvForecastTime: TextView = itemView.findViewById(R.id.tvForecastTime)
        val ivForecastIcon: ImageView = itemView.findViewById(R.id.ivForecastIcon)
        val tvForecastTemp: TextView = itemView.findViewById(R.id.tvForecastTemp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherForecastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast, parent, false)
        return WeatherForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeatherForecastViewHolder, position: Int) {

        val currentItem = threeHourForecasts[position]

        holder.apply {

            val sdf = SimpleDateFormat("HH:mm")
            val hour: String = sdf.format((currentItem.dt + timezone) * 1000L)
            tvForecastTime.text = hour

            val icon = currentItem.weather[0].icon
            ivForecastIcon.load("https://openweathermap.org/img/wn/$icon@2x.png")

            var roundTemp = round(currentItem.main.temp)
            roundTemp = if (roundTemp == -0.0) 0.0 else roundTemp
            tvForecastTemp.text = itemView.context.resources.getString(R.string.ei_temp, String.format("%.0f", roundTemp))

        }

    }

    override fun getItemCount(): Int {
        return threeHourForecasts.size
    }

    fun setData(threeHourForecasts: List<Forecast>, timezone: Int) {
        this.threeHourForecasts = threeHourForecasts
        this.timezone = timezone
        notifyDataSetChanged()
    }
}