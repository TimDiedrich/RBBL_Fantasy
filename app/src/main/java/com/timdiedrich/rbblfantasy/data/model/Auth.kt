package com.timdiedrich.rbblfantasy.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name="username") val username:String,
    @Json(name="email")val email:String,
    @Json(name="password") val pw:String
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name="message") val message:String?,
    @Json(name="token") val token: String,
    @Json(name="user") val user: User
)
@JsonClass(generateAdapter = true)
data class User(
    @Json(name="id") val id: Int,
    @Json(name="username") val username: String,
    @Json(name="email") val email: String,
    @Json(name="created_at") val createdAt:String? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name="username") val username:String,
    @Json(name="password") val pw:String
)