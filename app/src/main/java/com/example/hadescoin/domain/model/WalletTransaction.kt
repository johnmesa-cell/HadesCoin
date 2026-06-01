package com.example.hadescoin.domain.model

data class WalletTransaction(
    val id:               String = "",
    val senderId:         String = "",
    val senderName:       String = "",
    val receiverId:       String = "",
    val receiverName:     String = "",
    val amount:           Double = 0.0,
    val type:             String = "TRANSFER",
    val direction:        String = "OUT",
    val timestamp:        String = "",
    // Campos exclusivos de retiro con código temporal
    val verificationCode: String = "",
    val withdrawalAmount: Double = 0.0,
    val expiresAt:        String = "",
    val source:           String = ""
)
