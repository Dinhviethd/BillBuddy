package com.example.billbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.Category
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.repo.CategoryRepository
import com.example.billbuddy.data.repo.ExpenseRepository
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Locale
import javax.inject.Inject

data class ExpenseUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val auth: FirebaseAuth,
) : ViewModel() {

    private val _expenseState = MutableStateFlow(ExpenseUiState())
    val expenseState: StateFlow<ExpenseUiState> = _expenseState.asStateFlow()

    private val _saveState = MutableStateFlow<Resource<Unit>?>(null)
    val saveState: StateFlow<Resource<Unit>?> = _saveState.asStateFlow()

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    init {
        observeExpenses()
        observeCategories()
    }

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun addExpense(date: String, categoryId: String, amount: Double, note: String) {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val parsedDate = try {
            sdf.parse(date)?.let { Timestamp(it) }
        } catch (_: Exception) {
            Timestamp.now()
        }

        val expense = Expense(
            date = parsedDate,
            categoryId = categoryId,
            amount = amount.toLong(),
            description = note,
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
                    _expenseState.value = _expenseState.value.copy(
                        isLoading = false,
                        expenses = result.data.orEmpty(),
                        errorMessage = null
                    )
                }
                is Resource.Error -> {
                    _expenseState.value = _expenseState.value.copy(
                        isLoading = false,
                        expenses = result.data.orEmpty(),
                        errorMessage = result.message
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun observeCategories() {
        val uid = auth.currentUser?.uid ?: return
        categoryRepository.getCategories(uid).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _expenseState.value = _expenseState.value.copy(
                        categories = result.data.orEmpty()
                    )
                }
                else -> {}
            }
        }.launchIn(viewModelScope)
    }
}
