
package com.flash.climora.presentation.weather

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flash.climora.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonSearch.setOnClickListener {
            viewModel.fetchWeather(binding.editCity.text.toString())
        }

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is WeatherUiState.Loading -> binding.textResult.text = "Loading..."
                    is WeatherUiState.Success -> binding.textResult.text =
                        "${state.weather.cityName}: ${state.weather.temperature}°C"
                    is WeatherUiState.Error -> binding.textResult.text = state.message
                    else -> {}
                }
            }
        }
    }
}
