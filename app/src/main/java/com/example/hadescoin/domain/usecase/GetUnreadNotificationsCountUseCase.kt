package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.NotificationRepository

class GetUnreadNotificationsCountUseCase(private val repository: NotificationRepository) {
    suspend operator fun invoke(phoneNumber: String): Int {
        if (phoneNumber.isBlank()) return 0
        return repository.getUnreadCount(phoneNumber)
    }
}

