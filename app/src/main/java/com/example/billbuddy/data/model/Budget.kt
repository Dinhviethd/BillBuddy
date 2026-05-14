package com.example.billbuddy.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

enum class BudgetPeriod {
    MONTHLY,
    WEEKLY,
    YEARLY
}

data class Budget(
    @DocumentId
    val documentId: String = "",
    val name: String = "",
    val amount: Long = 0L,
    val period: BudgetPeriod = BudgetPeriod.MONTHLY,
    val categoryId: String = "",
    val userId: String = "",
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null
)
