package com.example.hadescoin.domain.model

data class AppNotification(
    val id: String = "",
    val phoneNumber: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "SYSTEM",
    val createdAt: String = "",
    val read: Boolean = false
)

