
package com.flash.climora.domain.usecase

import com.flash.climora.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {

    suspend operator fun invoke(city: String) =
        repository.getWeather(city)

    suspend operator fun invoke(
        lat: Double,
        lon: Double
    ) = repository.getWeatherByCoordinates(lat, lon)
}
