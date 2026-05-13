package com.example.billbuddy.data.model

data class Expense(
    val id: String = "",
    val date: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val note: String = "",
    val createdAt: Long = 0L
)

