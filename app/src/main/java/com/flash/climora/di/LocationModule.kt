package com.flash.climora.di

import android.content.Context
import com.flash.climora.data.location.LocationProviderImpl
import com.flash.climora.domain.location.LocationProvider
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {
    @Suppress("unused")
    @Binds
    abstract fun bindLocationProvider(
        impl: LocationProviderImpl
    ): LocationProvider

    companion object {
        @Provides
        @Singleton
        fun provideFusedLocationClient(
            @ApplicationContext context: Context
        ): FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)
    }
}