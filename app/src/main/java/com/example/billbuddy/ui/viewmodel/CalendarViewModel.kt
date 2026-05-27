package com.example.billbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.Category
import com.example.billbuddy.data.model.CategoryType
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.repo.CategoryRepository
import com.example.billbuddy.data.repo.ExpenseRepository
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class CalendarUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val totalDayAmount: Double = 0.0,
    val selectedDay: String = "",
    val selectedMonth: String = "",
    val selectedYear: String = "",
    val errorMessage: String? = null,
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        val calendar = Calendar.getInstance()
        _uiState.update {
            it.copy(
                selectedDay = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.DAY_OF_MONTH)),
                selectedMonth = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1),
                selectedYear = calendar.get(Calendar.YEAR).toString()
            )
        }
        observeData()
    }

    private fun observeData() {
        val uid = auth.currentUser?.uid ?: return

        combine(
            expenseRepository.observeExpenses(),
            categoryRepository.getCategories(uid)
        ) { expenseRes, categoryRes ->
            when {
                (expenseRes is Resource.Loading || categoryRes is Resource.Loading) -> {
                    _uiState.update { it.copy(isLoading = true) }
                }
                expenseRes is Resource.Success && categoryRes is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            expenses = expenseRes.data.orEmpty(),
                            categories = categoryRes.data.orEmpty(),
                            errorMessage = null
                        )
                    }
                    filterExpenses()
                }
                expenseRes is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = expenseRes.message) }
                }
                categoryRes is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = categoryRes.message) }
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

        val categoryMap = state.categories.associateBy { it.documentId }

        val totalIncome = filtered
            .filter { categoryMap[it.categoryId]?.type == CategoryType.INCOME }
            .sumOf { it.amount }.toDouble()

        val totalExpense = filtered
            .filter { categoryMap[it.categoryId]?.type == CategoryType.EXPENSE }
            .sumOf { it.amount }.toDouble()

        _uiState.update {
            it.copy(
                filteredExpenses = filtered,
                totalDayAmount = totalIncome - totalExpense
            )
        }
    }

    fun formatTime(timestamp: Timestamp?): String {
        val date = timestamp?.toDate() ?: return ""
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }
}