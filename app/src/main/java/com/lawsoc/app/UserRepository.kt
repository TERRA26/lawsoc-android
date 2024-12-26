package com.lawsoc.app

import retrofit2.HttpException
import java.io.IOException

class UserRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun login(email: String, password: String): NetworkResult<AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.code == "success") {
                NetworkResult.Success(response)
            } else {
                NetworkResult.Error(response.message)
            }
        } catch (e: HttpException) {
            NetworkResult.Error("Server error: ${e.message}")
        } catch (e: IOException) {
            NetworkResult.Error("Please check your internet connection")
        } catch (e: Exception) {
            NetworkResult.Error("Login failed: ${e.message}")
        }
    }
}