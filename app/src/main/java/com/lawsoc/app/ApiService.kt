package com.lawsoc.app

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("chat")
    suspend fun sendMessage(@Body request: ChatRequest): ChatResponse

    @POST("wp-json/lawsoc/v1/auth")
    suspend fun login(@Body loginRequest: LoginRequest): AuthResponse
}