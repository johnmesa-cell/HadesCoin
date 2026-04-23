package com.example.hadescoin.domain.model

data class WalletTransaction(
    val id: String,
    val amount: Double,
    val type: String,
    val createdAt: Long
)

