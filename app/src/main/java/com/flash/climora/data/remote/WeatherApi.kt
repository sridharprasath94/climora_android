package com.flash.climora.data.remote

import com.flash.climora.data.remote.dto.WeatherDto
import retrofit2.http.GET
import retrofit2.http.Query


interface WeatherApi {
    @GET("current.json")
    suspend fun getWeather(
        @Query("key") key: String,
        @Query("q") city: String
    ): WeatherDto


    @GET("current.json")
    suspend fun getWeatherByCoordinates(
        @Query("key") key: String,
        @Query("q") latLong: String
    ): WeatherDto
}
