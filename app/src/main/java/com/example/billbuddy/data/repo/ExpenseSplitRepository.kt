package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.model.ExpenseSplit
import com.example.billbuddy.utils.Resource
import kotlinx.coroutines.flow.Flow

interface ExpenseSplitRepository {
    fun splitExpense(expense: Expense, splits: List<ExpenseSplit>): Flow<Resource<Unit>>
    fun observeSplitsByExpense(expenseId: String): Flow<Resource<List<ExpenseSplit>>>
    fun observeUserSplitsInGroup(uid: String, groupId: String): Flow<Resource<List<ExpenseSplit>>>
    fun observeAllSettledSplitsInGroup(groupId: String): Flow<Resource<List<ExpenseSplit>>>
    fun settleSplit(splitId: String): Flow<Resource<Unit>>
}
