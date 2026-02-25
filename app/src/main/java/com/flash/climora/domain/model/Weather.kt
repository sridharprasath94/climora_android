package com.flash.climora.domain.model

enum class WeatherTheme {
    DAY,
    NIGHT
}

data class Weather(
    val cityName: String,
    val region: String,
    val country: String,
    val temperature: Double,
    val feelsLike: String,
    val humidity: String,
    val conditionText: String,
    val conditionCode: Int,
    val isDay: Boolean,
    val weatherTheme: WeatherTheme = if (isDay) WeatherTheme.DAY else WeatherTheme.NIGHT
)
