package com.wearcast.app.data.model.weathercurrent


import com.google.gson.annotations.SerializedName

data class Rain(
    @SerializedName("1h")
    val h: Double
)