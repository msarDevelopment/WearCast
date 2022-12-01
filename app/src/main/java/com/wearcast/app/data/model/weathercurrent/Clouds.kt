package com.wearcast.app.data.model.weathercurrent


import com.google.gson.annotations.SerializedName

data class Clouds(
    @SerializedName("all")
    val all: Int
)