package com.flash.climora.presentation.weather

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.flash.climora.R
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

        // Trigger search when user presses enter on keyboard
        binding.editCity.setOnEditorActionListener { _, _, _ ->
            val city = binding.editCity.text.toString()
            if (city.isNotBlank()) {
                viewModel.fetchWeather(city)
            }
            true
        }

        observeState()
    }

    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {

                    is WeatherUiState.Loading -> {
                        binding.textTemperature.text = "..."
                        binding.textCity.text = "Loading"
                    }

                    is WeatherUiState.Success -> {
                        val weather = state.weather

                        binding.textTemperature.text = "${weather.temperature}°C"
                        binding.textCity.text = weather.cityName

                        // Set weather icon (ensure you map icon properly in domain layer)
//                        binding.imageCondition.setImageResource(weather.conditionCode)

                        // Switch background based on day/night
                        if (weather.isDay) {
                            binding.backgroundImage.setImageResource(R.drawable.day_image)
                            binding.backgroundOverlay.alpha = 0.15f
                        } else {
                            binding.backgroundImage.setImageResource(R.drawable.night_image)
                            binding.backgroundOverlay.alpha = 0.35f
                        }
                    }

                    is WeatherUiState.Error -> {
                        binding.textTemperature.text = "--"
                        binding.textCity.text = state.message
                    }

                    else -> Unit
                }
            }
        }
    }
}
