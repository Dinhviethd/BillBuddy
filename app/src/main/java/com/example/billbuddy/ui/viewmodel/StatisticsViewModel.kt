package com.example.billbuddy.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.repo.AuthRepository
import com.example.billbuddy.data.repo.ExpenseRepository
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@Immutable
data class CategorySummary(
    val name: String,
    val amount: Double
)

@Immutable
data class StatisticsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val fromDate: LocalDate = LocalDate.now(),
    val toDate: LocalDate = LocalDate.now(),
    val totalAmount: Double = 0.0,
    val expenseCount: Int = 0,
    val categorySummaries: List<CategorySummary> = emptyList(),
    val hasData: Boolean = false
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val zoneId: ZoneId = ZoneId.systemDefault()
    private var allExpenses: List<Expense> = emptyList()

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        observeExpenses()
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

    private fun observeExpenses() {
        val userId = authRepository.currentUser?.uid
        if (userId.isNullOrBlank()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    totalAmount = 0.0,
                    expenseCount = 0,
                    categorySummaries = emptyList(),
                    hasData = false
                )
            }
            return
        }

        expenseRepository.observeExpenses(userId)
            .onEach { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
                    }

                    is Resource.Success -> {
                        allExpenses = resource.data ?: emptyList()
                        _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                        recalculate()
                    }

                    is Resource.Error -> {
                        allExpenses = emptyList()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = resource.message,
                                totalAmount = 0.0,
                                expenseCount = 0,
                                categorySummaries = emptyList(),
                                hasData = false
                            )
                        }
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun recalculate() {
        val currentState = _uiState.value
        val startDate = minOf(currentState.fromDate, currentState.toDate)
        val endDate = maxOf(currentState.fromDate, currentState.toDate)

        val filteredExpenses = allExpenses.filter { expense ->
            val expenseDate = expense.date?.toLocalDate(zoneId)
            expenseDate != null && !expenseDate.isBefore(startDate) && !expenseDate.isAfter(endDate)
        }.sortedByDescending { it.date?.seconds ?: 0L }

        val categorySummaries = filteredExpenses
            .groupBy { expense ->
                expense.categoryId.takeIf { it.isNotBlank() }
                    ?: expense.description.takeIf { it.isNotBlank() }
                    ?: "Khác"
            }
            .map { (categoryName, items) ->
                CategorySummary(
                    name = categoryName,
                    amount = items.sumOf { it.amount }
                )
            }
            .sortedByDescending { it.amount }

        _uiState.update {
            it.copy(
                totalAmount = filteredExpenses.sumOf { it.amount },
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

