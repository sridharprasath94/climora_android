package com.flash.climora.data.remote.error

import com.flash.climora.domain.error.DomainError

object NetworkErrorMapper {

    fun fromException(e: Exception): NetworkError {
        return when (e) {
            is retrofit2.HttpException -> {
                when (e.code()) {
                    403 -> NetworkError.RateLimitExceeded
                    else -> NetworkError.InvalidStatusCode(e.code())
                }
            }

            is java.io.IOException -> NetworkError.Network(e)

            is com.google.gson.JsonParseException ->
                NetworkError.Decoding(e)

            else -> NetworkError.InvalidResponse
        }
    }

    fun NetworkError.toDomain(): DomainError {
        return when (this) {
            is NetworkError.Network ->
                DomainError.NetworkUnavailable

            is NetworkError.RateLimitExceeded ->
                DomainError.RateLimitExceeded

            is NetworkError.InvalidStatusCode ->
                DomainError.InvalidRequest

            else ->
                DomainError.Unknown
        }
    }
}