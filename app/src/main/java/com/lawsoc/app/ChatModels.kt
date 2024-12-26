// ChatModels.kt
package com.lawsoc.app

data class ChatMessage(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatRequest(
    val query: String
)

data class ChatResponse(
    val response: String
)

