package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Budget
import com.example.billbuddy.utils.Resource
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgets(userId: String): Flow<Resource<List<Budget>>>
    fun addBudget(budget: Budget): Flow<Resource<Unit>>
    fun updateBudget(budget: Budget): Flow<Resource<Unit>>
    fun deleteBudget(budgetId: String): Flow<Resource<Unit>>
}
