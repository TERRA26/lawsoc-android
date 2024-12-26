package com.lawsoc.app.api

import com.lawsoc.app.models.LoginResponse
import com.lawsoc.app.models.UserResponse
import retrofit2.Response
import retrofit2.http.*

interface WordPressApiService {
    @POST("wp-json/jwt-auth/v1/token")
    @FormUrlEncoded
    suspend fun loginUser(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @GET("wp-json/wp/v2/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") token: String
    ): Response<UserResponse>
}