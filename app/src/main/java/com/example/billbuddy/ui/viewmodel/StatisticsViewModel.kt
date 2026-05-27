package com.example.billbuddy.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.Category
import com.example.billbuddy.data.model.CategoryType
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.repo.AuthRepository
import com.example.billbuddy.data.repo.CategoryRepository
import com.example.billbuddy.data.repo.ExpenseRepository
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class CategorySummary(
    val name: String,
    val amount: Double,
)

@Immutable
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val fromDate: LocalDate = LocalDate.now(),
    val toDate: LocalDate = LocalDate.now(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val expenseCount: Int = 0,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val hasData: Boolean = false,
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private var allExpenses: List<Expense> = emptyList()
    private var categories: List<Category> = emptyList()

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        observeData()
    }

    fun setTodayRange() {
        val today = LocalDate.now()
        _uiState.update {
            it.copy(fromDate = today, toDate = today)
        }
        recalculate()
    }

    fun setFromDate(date: LocalDate) {
        _uiState.update { it.copy(fromDate = date) }
        recalculate()
    }

    fun setToDate(date: LocalDate) {
        _uiState.update { it.copy(toDate = date) }
        recalculate()
    }

    fun refresh() {
        recalculate()
    }

    private fun observeData() {
        val userId = authRepository.currentUser?.uid
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "User not logged in",
                    hasData = false
                )
            }
            return
        }

        combine(
            expenseRepository.observeExpenses(),
            categoryRepository.getCategories(userId)
        ) { expenseRes, categoryRes ->
            when {
                (expenseRes is Resource.Loading || categoryRes is Resource.Loading) -> {
                    _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                }
                expenseRes is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = expenseRes.message) }
                }
                categoryRes is Resource.Error -> {
                    _uiState.update { it.copy(isLoading = false, errorMessage = categoryRes.message) }
                }
                expenseRes is Resource.Success && categoryRes is Resource.Success -> {
                    allExpenses = expenseRes.data.orEmpty()
                    categories = categoryRes.data.orEmpty()
                    _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                    recalculate()
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun recalculate() {
        val currentState = _uiState.value
        val startDate = if (currentState.fromDate.isBefore(currentState.toDate) || currentState.fromDate == currentState.toDate) currentState.fromDate else currentState.toDate
        val endDate = if (currentState.fromDate.isAfter(currentState.toDate) || currentState.fromDate == currentState.toDate) currentState.fromDate else currentState.toDate

        val filteredExpenses = allExpenses.filter { expense ->
            val expenseDate = expense.date?.toLocalDate(zoneId)
            expenseDate != null && !expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate)
        }

        val categoryMap = categories.associateBy { it.documentId }

        val totalIncome = filteredExpenses
            .filter { categoryMap[it.categoryId]?.type == CategoryType.INCOME }
            .sumOf { it.amount }.toDouble()

        val totalExpense = filteredExpenses
            .filter { categoryMap[it.categoryId]?.type == CategoryType.EXPENSE }
            .sumOf { it.amount }.toDouble()

        val categorySummaries = filteredExpenses
            .filter { categoryMap[it.categoryId]?.type == CategoryType.EXPENSE }
            .groupBy { it.categoryId }
            .map { (categoryId, items) ->
                val categoryName = categoryMap[categoryId]?.name ?: "Khác"
                CategorySummary(
                    name = categoryName,
                    amount = items.sumOf { it.amount }.toDouble()
                )
            }
            .sortedByDescending { it.amount }

        _uiState.update { state ->
            state.copy(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                balance = totalIncome - totalExpense,
                expenseCount = filteredExpenses.size,
                categorySummaries = categorySummaries,
                hasData = filteredExpenses.isNotEmpty(),
                errorMessage = null,
                isLoading = false
            )
        }
    }

    private fun Timestamp.toLocalDate(zoneId: ZoneId): LocalDate {
        return toDate().toInstant().atZone(zoneId).toLocalDate()
    }
}
