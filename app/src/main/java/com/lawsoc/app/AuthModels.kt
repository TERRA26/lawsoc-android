package com.lawsoc.app

data class AuthResponse(
    val code: String,
    val message: String,
    val data: AuthData?
)

data class AuthData(
    val token: String,
    val user: UserData
)

data class UserData(
    val id: Int,
    val email: String,
    val display_name: String,
    val roles: List<String>
)

data class LoginRequest(
    val username: String,
    val password: String
)