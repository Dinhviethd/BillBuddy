package com.example.billbuddy.data.model

import com.google.firebase.firestore.DocumentId

enum class CategoryType {
    INCOME,
    EXPENSE
}

data class Category(
    @DocumentId
    val documentId: String = "",
    val name: String = "",
    val icon: String = "",
    val color: String = "",
    val type: CategoryType = CategoryType.EXPENSE,
    val userId: String = ""
)
