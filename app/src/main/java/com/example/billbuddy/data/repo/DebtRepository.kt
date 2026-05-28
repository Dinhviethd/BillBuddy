package com.example.billbuddy.data.repo

import com.example.billbuddy.data.model.Debt
import com.example.billbuddy.utils.Resource
import kotlinx.coroutines.flow.Flow

interface DebtRepository {
    fun getDebtsByCreditor(userId: String): Flow<Resource<List<Debt>>>
    fun getDebtsByDebtor(userId: String): Flow<Resource<List<Debt>>>
    fun getDebtById(debtId: String): Flow<Resource<Debt>>
    fun addDebt(debt: Debt): Flow<Resource<Unit>>
    fun deleteDebt(debtId: String): Flow<Resource<Unit>>
    fun updateDebtStatus(debtId: String, status: com.example.billbuddy.data.model.DebtStatus): Flow<Resource<Unit>>
}
