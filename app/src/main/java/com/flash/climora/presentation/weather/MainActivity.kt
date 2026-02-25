package com.flash.climora.presentation.weather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.flash.climora.R
import com.flash.climora.databinding.ActivityMainBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

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

        binding.buttonLocation.setOnClickListener {
            requestLocationAndFetchWeather()
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
                        val iconRes = weatherIconRes(
                            conditionCode = weather.conditionCode,
                            isDay = weather.isDay
                        )
                        binding.imageCondition.visibility = android.view.View.VISIBLE
                        binding.imageCondition.setImageResource(iconRes)

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
                        binding.imageCondition.visibility = android.view.View.INVISIBLE
                    }

                    else -> Unit
                }
            }
        }
    }

    private fun requestLocationAndFetchWeather() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        } else {
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                location?.let {
                    viewModel.fetchWeatherByCoordinates(it.latitude, it.longitude)
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentLocation()
        }
    }
}
