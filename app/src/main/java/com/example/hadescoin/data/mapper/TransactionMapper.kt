package com.example.hadescoin.data.mapper

import com.example.hadescoin.domain.model.WalletTransaction

fun Map<String, Any>.toWalletTransaction(): WalletTransaction {
    return WalletTransaction(
        id = this["id"] as? String ?: "",
        amount = (this["amount"] as? Number)?.toDouble() ?: 0.0,
        type = this["type"] as? String ?: "UNKNOWN",
        createdAt = (this["createdAt"] as? Number)?.toLong() ?: 0L
    )
}

