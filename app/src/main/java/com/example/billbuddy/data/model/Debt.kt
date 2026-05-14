package com.example.billbuddy.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class DebtStatus {
    PENDING,
    SETTLED
}

data class Debt(
    @DocumentId
    val documentId: String = "",
    val amount: Long = 0L,
    val description: String = "",
    val note: String = "",
    val creditorId: String = "",
    val debtorId: String = "",
    val status: DebtStatus = DebtStatus.PENDING,
    val dueDate: Timestamp? = null,
    val createdAt: Timestamp? = null
)
