package com.example.billbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.repo.ExpenseRepository
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val totalAmount: Double = 0.0,
    val selectedDay: String = "",
    val selectedMonth: String = "",
    val selectedYear: String = "",
    val errorMessage: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private val _editState = MutableStateFlow<Resource<Unit>?>(null)
    val editState: StateFlow<Resource<Unit>?> = _editState.asStateFlow()

    init {
        val calendar = Calendar.getInstance()
        _uiState.update { 
            it.copy(
                selectedDay = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.DAY_OF_MONTH)),
                selectedMonth = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1),
                selectedYear = calendar.get(Calendar.YEAR).toString()
            )
        }
        observeExpenses()
    }

    private fun observeExpenses() {
        expenseRepository.observeExpenses().onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                is Resource.Success -> {
                    val allExpenses = result.data.orEmpty()
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            expenses = allExpenses,
                            errorMessage = null
                        )
                    }
                    filterExpenses()
                }
                is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = result.message) }
                }
            }
        }.launchIn(viewModelScope)
    }

    fun setDateFilter(day: String, month: String, year: String) {
        _uiState.update { 
            it.copy(
                selectedDay = day,
                selectedMonth = month,
                selectedYear = year
            )
        }
        filterExpenses()
    }

    private fun filterExpenses() {
        val state = _uiState.value
        val filterDate = "${state.selectedYear}-${state.selectedMonth}-${state.selectedDay}"
        
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val filtered = state.expenses.filter { expense ->
            expense.date?.toDate()?.let { sdf.format(it) == filterDate } ?: false
        }
        
        val totalIncome = filtered.filter { it.type == "INCOME" }.sumOf { it.amount }.toDouble()
        val totalExpense = filtered.filter { it.type == "EXPENSE" }.sumOf { it.amount }.toDouble()
        val netBalance = totalIncome - totalExpense
        
        _uiState.update { 
            it.copy(
                filteredExpenses = filtered,
                totalAmount = netBalance
            )
        }
    }

    fun updateExpense(expense: Expense) {
        expenseRepository.updateExpense(expense).onEach { result ->
            _editState.value = result
        }.launchIn(viewModelScope)
    }

    fun deleteExpense(expenseId: String) {
        expenseRepository.deleteExpense(expenseId).onEach { result ->
            _editState.value = result
        }.launchIn(viewModelScope)
    }

    fun clearEditState() {
        _editState.value = null
    }

    fun formatTime(timestamp: Timestamp?): String {
        val date = timestamp?.toDate() ?: return ""
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }
}
