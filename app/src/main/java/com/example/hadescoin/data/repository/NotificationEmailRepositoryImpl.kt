package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseNotificationDataSource
import com.example.hadescoin.domain.repository.NotificationEmailRepository

class NotificationEmailRepositoryImpl(
    private val dataSource: FirebaseNotificationDataSource = FirebaseNotificationDataSource()
) : NotificationEmailRepository {
    override suspend fun queueNotificationEmail(
        phoneNumber: String,
        toEmail: String,
        subject: String,
        body: String
    ): Boolean {
        return dataSource.queueNotificationEmail(phoneNumber, toEmail, subject, body)
    }
}

