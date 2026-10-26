package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiErrorResponse(
    @Json(name = "status") val status: String?,
    @Json(name = "statusCode") val statusCode: Int?,
    @Json(name = "error") val error: String? // Contains the specific error message.
)