package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.NotificationEmailRepository

class QueueNotificationEmailUseCase(private val repository: NotificationEmailRepository) {
    suspend operator fun invoke(
        phoneNumber: String,
        toEmail: String,
        subject: String,
        body: String
    ): Boolean {
        if (phoneNumber.isBlank() || toEmail.isBlank() || subject.isBlank() || body.isBlank()) return false
        return repository.queueNotificationEmail(phoneNumber, toEmail, subject, body)
    }
}

