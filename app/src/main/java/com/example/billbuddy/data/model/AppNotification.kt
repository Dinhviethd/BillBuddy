package com.example.billbuddy.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class NotificationType {
    INFO,           // Sắp đến hạn (1 ngày)
    WARNING,        // Đến hạn hôm nay
    URGENT,         // Quá hạn
    BUDGET_EXCEEDED, // Vượt hạn mức chi tiêu
    SYSTEM          // Thông báo hệ thống khác
}

data class AppNotification(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val title: String = "", // Tiêu đề thông báo (ví dụ: "Cảnh báo hạn mức")
    val message: String = "",
    val type: NotificationType = NotificationType.INFO,
    val amount: Long = 0L, // Số tiền liên quan (nếu có)
    val createdAt: Timestamp = Timestamp.now(),
    val isRead: Boolean = false,
    val relatedId: String = "", // ID liên quan (có thể là debtId, budgetId, v.v.)
    val metadata: Map<String, String> = emptyMap() // Lưu trữ thêm thông tin tùy biến nếu cần
)
