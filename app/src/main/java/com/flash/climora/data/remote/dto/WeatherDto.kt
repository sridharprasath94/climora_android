package com.flash.climora.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.flash.climora.domain.model.Weather

data class WeatherDto(
    val location: LocationDto,
    val current: CurrentDto
)

data class LocationDto(
    val name: String,
    val region: String,
    val country: String
)

data class CurrentDto(
    @SerializedName("is_day")
    val isDay: Int,

    @SerializedName("temp_c")
    val tempC: Double,

    @SerializedName("feelslike_c")
    val feelsLikeC: Double,

    val humidity: Int,
    val uv: Double,
    val condition: ConditionDto
)

data class ConditionDto(
    val text: String,
    val icon: String,
    val code: Int
)

fun WeatherDto.toDomain(): Weather {
    return Weather(
        cityName = location.name,
        region = location.region,
        country = location.country,
        temperature = current.tempC,
        feelsLike = "Feels like ${current.feelsLikeC.toInt()}°C",
        humidity = "${current.humidity}%",
        conditionText = current.condition.text,
        conditionCode = current.condition.code,
        isDay = current.isDay == 1
    )
}

