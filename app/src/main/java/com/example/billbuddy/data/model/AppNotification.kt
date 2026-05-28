package com.example.billbuddy.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class NotificationType {
    INFO,
    WARNING,
    URGENT,
    BUDGET_EXCEEDED,
    SYSTEM
}

data class AppNotification(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val amount: Long = 0L,
    val createdAt: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val relatedId: String = "",
    val metadata: Map<String, String> = emptyMap()
)
