
package com.flash.climora.domain.repository

import com.flash.climora.domain.model.Weather

interface WeatherRepository {
    suspend fun getWeather(city: String): Weather
    suspend fun  getWeatherByCoordinates(lat: Double, lon: Double): Weather
}
