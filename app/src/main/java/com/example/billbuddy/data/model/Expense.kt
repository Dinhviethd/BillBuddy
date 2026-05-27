package com.example.billbuddy.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Expense(
    @DocumentId
    val documentId: String = "",
    val amount: Long = 0L,
    val description: String = "",
    val date: Timestamp? = null,
    val categoryId: String = "",
    val type: String = "EXPENSE", // "EXPENSE" or "INCOME"
    val userId: String = "",
    val groupId: String? = null,
    val splitMethod: String? = null,
    val createdAt: Timestamp? = null
)
