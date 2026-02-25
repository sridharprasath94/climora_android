package com.flash.climora.data.remote.error

sealed class NetworkError {

    object InvalidResponse : NetworkError()

    data class InvalidStatusCode(val code: Int) : NetworkError()

    object RateLimitExceeded : NetworkError()

    data class Network(val throwable: Throwable) : NetworkError()

    data class Decoding(val throwable: Throwable) : NetworkError()

    fun toUserMessage(): String {
        return when (this) {
            InvalidResponse -> "Invalid server response."
            is InvalidStatusCode -> "Server returned error code: $code"
            RateLimitExceeded -> "API rate limit exceeded. Try again later."
            is Network -> throwable.localizedMessage ?: "Network error"
            is Decoding -> "Failed to parse server response."
        }
    }
}