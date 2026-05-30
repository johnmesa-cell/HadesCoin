package com.example.hadescoin.domain.repository

import com.example.hadescoin.domain.model.AppNotification

interface NotificationRepository {
    suspend fun saveNotification(notification: AppNotification): Boolean
    suspend fun getNotificationsByPhone(phoneNumber: String): List<AppNotification>
    suspend fun markAsRead(phoneNumber: String, notificationId: String): Boolean
    suspend fun getUnreadCount(phoneNumber: String): Int
}

