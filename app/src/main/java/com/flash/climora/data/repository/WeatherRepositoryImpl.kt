package com.flash.climora.data.repository

import com.flash.climora.BuildConfig
import com.flash.climora.data.remote.WeatherApi
import com.flash.climora.data.remote.dto.toDomain
import com.flash.climora.domain.model.Weather
import com.flash.climora.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi
) : WeatherRepository {

    override suspend fun getWeather(city: String): Weather {
        val dto = api.getWeather(BuildConfig.API_KEY, city)
        return dto.toDomain()
    }

    override suspend fun getWeatherByCoordinates(
        lat: Double,
        lon: Double
    ): Weather {
        val latLong = "$lat,$lon"
        val dto = api.getWeatherByCoordinates(BuildConfig.API_KEY, latLong)
        return dto.toDomain()
    }
}
