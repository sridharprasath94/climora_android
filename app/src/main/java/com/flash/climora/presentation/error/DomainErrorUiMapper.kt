package com.flash.climora.presentation.error

import com.flash.climora.domain.error.DomainError

fun DomainError.toUiMessage(): String = when (this) {
    DomainError.NetworkUnavailable -> "No internet connection"
    DomainError.RateLimitExceeded -> "API rate limit exceeded"
    DomainError.InvalidRequest -> "Invalid request"
    DomainError.Unknown -> "Something went wrong"
}