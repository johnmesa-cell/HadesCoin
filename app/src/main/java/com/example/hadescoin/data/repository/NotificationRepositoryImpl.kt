package com.example.hadescoin.data.repository

import com.example.hadescoin.data.datasource.FirebaseNotificationDataSource
import com.example.hadescoin.domain.model.AppNotification
import com.example.hadescoin.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val dataSource: FirebaseNotificationDataSource = FirebaseNotificationDataSource()
) : NotificationRepository {

    override suspend fun saveNotification(notification: AppNotification): Boolean {
        val data = mapOf(
            "phoneNumber" to notification.phoneNumber,
            "title" to notification.title,
            "message" to notification.message,
            "type" to notification.type,
            "createdAt" to notification.createdAt,
            "read" to notification.read
        )
        return dataSource.saveNotification(notification.phoneNumber, data)
    }

    override suspend fun getNotificationsByPhone(phoneNumber: String): List<AppNotification> {
        return dataSource.getNotifications(phoneNumber)
            .map { snapshot ->
                AppNotification(
                    id = snapshot.key ?: "",
                    phoneNumber = phoneNumber,
                    title = snapshot.child("title").getValue(String::class.java) ?: "",
                    message = snapshot.child("message").getValue(String::class.java) ?: "",
                    type = snapshot.child("type").getValue(String::class.java) ?: "SYSTEM",
                    createdAt = snapshot.child("createdAt").getValue(String::class.java) ?: "",
                    read = snapshot.child("read").getValue(Boolean::class.java) ?: false
                )
            }
            .sortedByDescending { it.createdAt }
    }

    override suspend fun markAsRead(phoneNumber: String, notificationId: String): Boolean {
        return dataSource.markAsRead(phoneNumber, notificationId)
    }

    override suspend fun getUnreadCount(phoneNumber: String): Int {
        return dataSource.getUnreadCount(phoneNumber)
    }
}

