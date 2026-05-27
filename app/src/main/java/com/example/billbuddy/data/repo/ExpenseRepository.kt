package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeExpenses(): Flow<Resource<List<Expense>>>
    fun addExpense(expense: Expense): Flow<Resource<Unit>>
}