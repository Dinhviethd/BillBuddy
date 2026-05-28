package com.example.billbuddy.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class SplitStatus {
    PENDING,
    SETTLED
}

data class ExpenseSplit(
    @DocumentId
    val documentId: String = "",
    val amount: Long = 0L,
    val description: String = "",
    val paidBy: String = "",
    val groupId: String = "",
    val expenseId: String = "",
    val status: SplitStatus = SplitStatus.PENDING,
    val createdAt: Timestamp? = null
)
