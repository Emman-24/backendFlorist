package com.floristeriaakasia.backend.controller.response

sealed class ApiResponse<out T> {

    data class Success<T>(
        val data: T,
        val message: String? = null
    ) : ApiResponse<T>()

    data class Error(
        val message: String,
        val errors: Map<String, String>? = null,
        val code: String? = null
    ) : ApiResponse<Nothing>()

    data class ValidationError(
        val message: String,
        val fieldErrors: Map<String, String>
    ) : ApiResponse<Nothing>()

}

fun <T> T.toSuccessResponse(message: String? = null) = ApiResponse.Success(this, message)

fun String.toErrorResponse(
    errors: Map<String, String>? = null,
    code: String? = null
) = ApiResponse.Error(this, errors, code)