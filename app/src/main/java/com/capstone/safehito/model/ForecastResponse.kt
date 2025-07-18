package com.capstone.safehito.model

data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt: Long,
    val main: MainData,
    val weather: List<WeatherData>,
    val wind: WindData
)

data class MainData(
    val temp: Float,
    val pressure: Int,
    val humidity: Int
)

data class WeatherData(
    val description: String,
    val icon: String
)

data class WindData(
    val speed: Float
)
