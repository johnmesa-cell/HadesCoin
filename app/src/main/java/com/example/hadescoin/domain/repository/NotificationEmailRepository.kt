package com.example.hadescoin.domain.repository

interface NotificationEmailRepository {
    suspend fun queueNotificationEmail(
        phoneNumber: String,
        toEmail: String,
        subject: String,
        body: String
    ): Boolean
}

