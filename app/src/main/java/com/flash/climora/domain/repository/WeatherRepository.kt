
package com.flash.climora.domain.repository

import com.flash.climora.core.Result
import com.flash.climora.domain.model.Weather

interface WeatherRepository {
    suspend fun getWeather(city: String): Result<Weather>
    suspend fun  getWeatherByCoordinates(lat: Double, lon: Double): Result<Weather>
}
