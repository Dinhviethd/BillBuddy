package com.example.billbuddy.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.repo.ExpenseRepository
import com.example.billbuddy.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject

data class ExpenseUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val errorMessage: String? = null
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _expenseState = MutableStateFlow(ExpenseUiState())
    val expenseState: StateFlow<ExpenseUiState> = _expenseState.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<Unit>?>(null)
    val saveState: StateFlow<Resource<Unit>?> = _saveState.asStateFlow()

    init {
        observeExpenses()
    }

    fun addExpense(date: String, category: String, amount: Double, note: String, type: String = "EXPENSE") {
        val parsedDate = try {
            val localDate = LocalDate.parse(date)
            val instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            Timestamp(Date.from(instant))
        } catch (e: Exception) {
            Timestamp.now()
        }

        val expense = Expense(
            date = parsedDate,
            categoryId = if (type == "INCOME") "Thu nhập" else category,
            amount = amount.toLong(),
            description = note,
            type = type,
            createdAt = Timestamp.now()
        )

        expenseRepository.addExpense(expense).onEach { result ->
            _saveState.value = result
        }.launchIn(viewModelScope)
    }

    fun clearSaveState() {
        _saveState.value = null
    }

    private fun observeExpenses() {
        expenseRepository.observeExpenses().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _expenseState.value = _expenseState.value.copy(isLoading = true, errorMessage = null)
                }
                is Resource.Success -> {
                    _expenseState.value = ExpenseUiState(
                        isLoading = false,
                        expenses = result.data.orEmpty(),
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    _expenseState.value = ExpenseUiState(
                        isLoading = false,
                        expenses = result.data.orEmpty(),
                        errorMessage = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}

