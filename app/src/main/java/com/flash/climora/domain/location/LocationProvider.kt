package com.flash.climora.domain.location

import com.flash.climora.domain.model.Coordinates

interface LocationProvider {
    suspend fun getCurrentLocation(): Result<Coordinates>
}