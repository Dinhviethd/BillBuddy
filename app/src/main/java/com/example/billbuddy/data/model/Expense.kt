package com.example.billbuddy.data.model

import com.google.firebase.Timestamp

data class Expense(
	val documentId: String = "",
	val amount: Double = 0.0,
	val description: String = "",
	val date: Timestamp? = null,
	val categoryId: String = "",
	val userId: String = "",
	val groupId: String? = null,
	val splitMethod: String? = null,
	val createdAt: Timestamp? = null
)

