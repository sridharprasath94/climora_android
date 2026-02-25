package com.flash.climora.presentation.weather

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    private var errorDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initially hide weather UI until we have real data
        setWeatherViewsVisible(false)

        // Search button click
        binding.buttonSearch.setOnClickListener {
            val city = binding.editCity.text.toString().trim()
            if (city.isNotBlank()) {
                viewModel.fetchWeather(city)
            }
        }

        // Trigger search when user presses the IME action (search/done)
        binding.editCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val city = binding.editCity.text.toString().trim()
                if (city.isNotBlank() && binding.buttonSearch.isEnabled) {
                    viewModel.fetchWeather(city)
                }
                true
            } else {
                false
            }
        }

        // Location button click
        binding.buttonLocation.setOnClickListener {
            requestLocationAndFetchWeather()
        }

        observeState()

        // On first launch, fetch weather by current location
        requestLocationAndFetchWeather()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    when (state) {
                        is WeatherUiState.Loading -> {
                            // Disable searching while loading
                            setSearchEnabled(false)
                            // Hide weather data while loading to avoid stale UI
                            setWeatherViewsVisible(false)
                        }

                        is WeatherUiState.Success -> {
                            dismissErrorDialog()

                            val weather = state.weather

                            binding.textTemperature.text = "${weather.temperature}°C"
                            binding.textCity.text = weather.cityName

                            val iconRes = weatherIconRes(
                                conditionCode = weather.conditionCode,
                                isDay = weather.isDay
                            )
                            binding.imageCondition.setImageResource(iconRes)

                            // Show only when we have data
                            setWeatherViewsVisible(true)
                            setSearchEnabled(true)

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
                            // Clear UI + hide weather details
                            clearWeatherUi()
                            setWeatherViewsVisible(false)
                            setSearchEnabled(true)

                            showErrorDialog(state.message)
                        }

                        else -> Unit
                    }
                }
            }
        }
    }

    private fun setWeatherViewsVisible(visible: Boolean) {
        val v = if (visible) View.VISIBLE else View.GONE
        binding.imageCondition.visibility = v
        binding.textTemperature.visibility = v
        binding.textCity.visibility = v
    }

    private fun clearWeatherUi() {
        binding.textTemperature.text = ""
        binding.textCity.text = ""
        binding.imageCondition.setImageDrawable(null)
    }

    private fun setSearchEnabled(enabled: Boolean) {
        binding.buttonSearch.isEnabled = enabled
        binding.editCity.isEnabled = enabled
        // Slight visual cue
        binding.buttonSearch.alpha = if (enabled) 1.0f else 0.4f
        binding.editCity.alpha = if (enabled) 1.0f else 0.7f
    }

    private fun showErrorDialog(message: String) {
        // Avoid stacking dialogs
        if (errorDialog?.isShowing == true) return

        errorDialog = AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message.ifBlank { "Something went wrong" })
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .create()

        errorDialog?.show()
    }

    private fun dismissErrorDialog() {
        errorDialog?.dismiss()
        errorDialog = null
    }

    private fun requestLocationAndFetchWeather() {
        // Disable searching while we try to obtain location + fetch
        setSearchEnabled(false)

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
            // Permission not granted; allow searching again
            setSearchEnabled(true)
            return
        }

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location == null) {
                    // Could not get location; allow searching
                    setSearchEnabled(true)
                    showErrorDialog("Unable to get current location. Please try again or search by city.")
                    return@addOnSuccessListener
                }

                viewModel.fetchWeatherByCoordinates(location.latitude, location.longitude)
            }.addOnFailureListener {
                setSearchEnabled(true)
                showErrorDialog("Unable to get current location. Please try again or search by city.")
            }
        } catch (e: SecurityException) {
            setSearchEnabled(true)
            showErrorDialog("Location permission error. Please allow location access.")
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
        } else if (requestCode == 1001) {
            // Permission denied; allow city search and show info
            setSearchEnabled(true)
            showErrorDialog("Location permission denied. You can still search by city.")
        }
    }

    override fun onDestroy() {
        dismissErrorDialog()
        super.onDestroy()
    }
}
