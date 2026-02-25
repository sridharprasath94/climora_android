package com.flash.climora.data.location

import android.annotation.SuppressLint
import com.flash.climora.domain.location.LocationProvider
import com.flash.climora.domain.model.Coordinates
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class LocationProviderImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient
) : LocationProvider {

    @SuppressLint("MissingPermission")
    override suspend fun getCurrentLocation(): Result<Coordinates> =
        suspendCancellableCoroutine { cont ->
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(
                        Result.success(
                            Coordinates(location.latitude, location.longitude)
                        )
                    )
                } else {
                    cont.resume(Result.failure(Exception("Location null")))
                }
            }.addOnFailureListener {
                cont.resume(Result.failure(it))
            }
        }
}