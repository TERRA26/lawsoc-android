package com.lawsoc.app

class ChatRepository {
    private val service = RetrofitClient.apiService

    suspend fun sendMessage(message: String): ChatResponse {
        return service.sendMessage(ChatRequest(query = message))
    }
}