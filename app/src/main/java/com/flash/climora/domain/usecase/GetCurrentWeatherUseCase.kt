package com.flash.climora.domain.usecase

import com.flash.climora.domain.model.Weather
import com.flash.climora.domain.repository.WeatherRepository
import com.flash.climora.core.Result
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {

    suspend operator fun invoke(city: String): Result<Weather> =
        repository.getWeather(city)

    suspend operator fun invoke(
        lat: Double,
        lon: Double
    ): Result<Weather> = repository.getWeatherByCoordinates(lat, lon)
}
