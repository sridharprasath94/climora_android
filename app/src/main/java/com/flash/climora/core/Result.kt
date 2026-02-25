package com.flash.climora.core

import com.flash.climora.domain.error.DomainError

sealed class Result<out T> {

    data class Success<T>(val data: T) : Result<T>()

    data class Error(val error: DomainError) : Result<Nothing>()
}