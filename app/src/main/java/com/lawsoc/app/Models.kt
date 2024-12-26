package com.lawsoc.app.models

data class LoginResponse(
    val token: String,
    val user_email: String,
    val user_nicename: String,
    val user_display_name: String
)

data class UserResponse(
    val id: Int,
    val name: String,
    val email: String,
    val roles: List<String>
)

data class LoginResult(
    val success: Boolean,
    val message: String,
    val data: LoginResponse? = null
)