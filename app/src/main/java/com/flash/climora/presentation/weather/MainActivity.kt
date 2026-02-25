package com.flash.climora.presentation.weather

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flash.climora.R
import com.flash.climora.databinding.ActivityMainBinding
import com.flash.climora.domain.model.Weather
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

    // Modern permission API (cleaner than onRequestPermissionsResult)
    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                fetchLocationWeather()
            } else {
                setSearchEnabled(true)
                showErrorDialog("Location permission denied. You can still search by city.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setWeatherViewsVisible(false)

        setupListeners()
        observeState()

        requestLocationWeather()
    }

    // --------------------------
    // Setup
    // --------------------------

    private fun setupListeners() {
        binding.buttonSearch.setOnClickListener {
            performCitySearch()
        }

        binding.editCity.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE
            ) {
                performCitySearch()
                true
            } else false
        }

        binding.buttonLocation.setOnClickListener {
            requestLocationWeather()
        }
    }

    private fun performCitySearch() {
        val city = binding.editCity.text.toString().trim()
        if (city.isNotBlank() && binding.buttonSearch.isEnabled) {
            viewModel.fetchWeather(city)
        }
    }

    // --------------------------
    // State Rendering
    // --------------------------

    @SuppressLint("SetTextI18n")
    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { renderState(it) }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun renderState(state: WeatherUiState) {
        when (state) {
            is WeatherUiState.Loading -> showLoading()

            is WeatherUiState.Success -> showSuccess(state.weather)

            is WeatherUiState.Error -> showError(state.message)

            else -> Unit
        }
    }

    private fun showLoading() {
        setSearchEnabled(false)
        setWeatherViewsVisible(false)
        binding.loadingOverlay.visibility = View.VISIBLE
    }

    private fun showSuccess(weather: Weather) {
        binding.loadingOverlay.visibility = View.GONE
        dismissErrorDialog()

        binding.textTemperature.text = "${weather.temperature}°C"
        binding.textCity.text = weather.cityName

        val iconRes = weatherIconRes(
            conditionCode = weather.conditionCode,
            isDay = weather.isDay
        )
        binding.imageCondition.setImageResource(iconRes)

        setWeatherViewsVisible(true)
        setSearchEnabled(true)

        if (weather.isDay) {
            binding.backgroundImage.setImageResource(R.drawable.day_image)
            binding.backgroundOverlay.alpha = 0.15f
        } else {
            binding.backgroundImage.setImageResource(R.drawable.night_image)
            binding.backgroundOverlay.alpha = 0.35f
        }
    }

    private fun showError(message: String) {
        binding.loadingOverlay.visibility = View.GONE
        clearWeatherUi()
        setWeatherViewsVisible(false)
        setSearchEnabled(true)
        showErrorDialog(message)
    }

    // --------------------------
    // Location
    // --------------------------

    private fun requestLocationWeather() {
        setSearchEnabled(false)

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocationWeather()
            }

            else -> locationPermissionLauncher.launch(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
    }

    private fun fetchLocationWeather() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            handleLocationFailure()
            return
        }

        try {
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location == null) {
                    handleLocationFailure()
                    return@addOnSuccessListener
                }

                viewModel.fetchWeatherByCoordinates(
                    location.latitude,
                    location.longitude
                )
            }.addOnFailureListener {
                handleLocationFailure()
            }
        } catch (e: SecurityException) {
            handleLocationFailure()
        }
    }

    private fun handleLocationFailure() {
        setSearchEnabled(true)
        showErrorDialog("Unable to get current location. Please try again or search by city.")
    }

    // --------------------------
    // UI Helpers
    // --------------------------

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
        binding.buttonSearch.alpha = if (enabled) 1.0f else 0.4f
        binding.editCity.alpha = if (enabled) 1.0f else 0.7f
    }

    private fun showErrorDialog(message: String) {
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

    override fun onDestroy() {
        dismissErrorDialog()
        super.onDestroy()
    }
}
