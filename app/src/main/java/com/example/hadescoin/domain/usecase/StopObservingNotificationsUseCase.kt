package com.example.hadescoin.domain.usecase

import com.example.hadescoin.domain.repository.NotificationRepository

class StopObservingNotificationsUseCase(private val repository: NotificationRepository) {
    operator fun invoke(phoneNumber: String, subscription: Any) {
        repository.stopObserving(phoneNumber, subscription)
    }
}

