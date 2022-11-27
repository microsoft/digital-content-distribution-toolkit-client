package com.msr.bine_sdk.network

import java.io.IOException

sealed class NetworkResponse<out T : Any, out U : Any> {
    /**
     * Success response with body
     */
    data class Success<T : Any>(val body: T, val headers: okhttp3.Headers) : NetworkResponse<T, Nothing>()

    /**
     * Failure response with body
     */
    data class ApiError<U : Any>(val body: U, val code: Int) : NetworkResponse<Nothing, U>()

    /**
     * Failure response with body
     */
    data class Api4xxError(val body: String, val code: Int) : NetworkResponse<Nothing, Nothing>()

    /**
     * Network error
     */
    data class NetworkError(val error: IOException) : NetworkResponse<Nothing, Nothing>()

    /**
     * For example, json parsing error
     */
    data class UnknownError(val error: Throwable?, val code: Int) : NetworkResponse<Nothing, Nothing>()
}
