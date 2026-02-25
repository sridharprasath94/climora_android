package com.flash.climora.presentation.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.climora.domain.usecase.GetCurrentWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeatherUseCase: GetCurrentWeatherUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<WeatherUiState>(WeatherUiState.Idle)
    val state: StateFlow<WeatherUiState> = _state

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            try {
                val weather = getWeatherUseCase(city)
                _state.value = WeatherUiState.Success(weather)
                Log.d("WeatherViewModel", "Weather fetched: $weather")
            } catch (e: Exception) {
                _state.value = WeatherUiState.Error("Something went wrong")
            }
        }
    }

    fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            try {
                val weather = getWeatherUseCase(lat, lon)
                _state.value = WeatherUiState.Success(weather)
                Log.d("WeatherViewModel", "Weather fetched: $weather")
            } catch (e: Exception) {
                _state.value = WeatherUiState.Error("Something went wrong")
            }
        }
    }
}
