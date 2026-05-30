package com.example.hadescoin.data.datasource

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.time.Instant

class FirebaseNotificationDataSource {

    private val database = FirebaseDatabase.getInstance()
    private val notificationsRef = database.getReference("notifications")
    private val emailQueueRef = database.getReference("emailQueue")

    suspend fun saveNotification(phoneNumber: String, notificationData: Map<String, Any>): Boolean {
        return try {
            val key = notificationsRef.child(phoneNumber).push().key ?: return false
            notificationsRef.child(phoneNumber).child(key).setValue(notificationData).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getNotifications(phoneNumber: String): List<DataSnapshot> {
        val snapshot = notificationsRef.child(phoneNumber).get().await()
        return snapshot.children.toList()
    }

    suspend fun markAsRead(phoneNumber: String, notificationId: String): Boolean {
        return try {
            notificationsRef.child(phoneNumber).child(notificationId).child("read").setValue(true).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun getUnreadCount(phoneNumber: String): Int {
        val snapshot = notificationsRef.child(phoneNumber).get().await()
        return snapshot.children.count {
            it.child("read").getValue(Boolean::class.java) != true
        }
    }

    suspend fun queueNotificationEmail(
        phoneNumber: String,
        toEmail: String,
        subject: String,
        body: String
    ): Boolean {
        return try {
            val payload = mapOf(
                "phoneNumber" to phoneNumber,
                "toEmail" to toEmail,
                "subject" to subject,
                "body" to body,
                "status" to "PENDING",
                "createdAt" to Instant.now().toString()
            )
            emailQueueRef.push().setValue(payload).await()
            true
        } catch (_: Exception) {
            false
        }
    }
}

