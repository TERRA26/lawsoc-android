package com.lawsoc.app

data class ChatSession(
    val id: Long,
    val name: String,
    val timestamp: Long,
    val lastMessage: String?
)