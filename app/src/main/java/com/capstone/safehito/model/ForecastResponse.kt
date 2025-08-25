package com.capstone.safehito.model

import androidx.annotation.Keep

@Keep
data class ForecastResponse(
    val list: List<ForecastItem>
)

@Keep data class ForecastItem(
    val dt: Long,
    val main: MainData,
    val weather: List<WeatherData>,
    val wind: WindData
)

@Keep data class MainData(
    val temp: Float,
    val pressure: Int,
    val humidity: Int
)

@Keep data class WeatherData(
    val description: String,
    val icon: String
)

@Keep data class WindData(
    val speed: Float
)
