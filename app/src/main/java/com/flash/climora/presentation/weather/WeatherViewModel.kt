package com.flash.climora.presentation.weather

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flash.climora.domain.usecase.GetCurrentWeatherUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.flash.climora.core.Result
import com.flash.climora.presentation.error.toUiMessage
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
            when (val result = getWeatherUseCase(city)) {

                is Result.Success -> {
                    _state.value = WeatherUiState.Success(result.data)
                }

                is Result.Error -> {
                    _state.value = WeatherUiState.Error(result.error.toUiMessage())
                }
            }
        }
    }

    fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = WeatherUiState.Loading
            when (val result = getWeatherUseCase(lat, lon)) {

                is Result.Success -> {
                    _state.value = WeatherUiState.Success(result.data)
                }

                is Result.Error -> {
                    _state.value = WeatherUiState.Error(result.error.toUiMessage())
                }
            }
        }
    }
}
