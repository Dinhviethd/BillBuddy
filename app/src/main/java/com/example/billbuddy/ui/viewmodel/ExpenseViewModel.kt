package com.example.billbuddy.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billbuddy.data.model.AppNotification
import com.example.billbuddy.data.model.Budget
import com.example.billbuddy.data.model.Category
import com.example.billbuddy.data.model.CategoryType
import com.example.billbuddy.data.model.Debt
import com.example.billbuddy.data.model.DebtStatus
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.data.model.NotificationType
import com.example.billbuddy.data.repo.BudgetRepository
import com.example.billbuddy.data.repo.CategoryRepository
import com.example.billbuddy.data.repo.DebtRepository
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import javax.inject.Inject

data class ExpenseUiState(
    val isLoading: Boolean = false,
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val budgets: List<Budget> = emptyList(),
    val pendingDebts: List<Debt> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val debtRepository: DebtRepository,
    private val budgetRepository: BudgetRepository,
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
        observeBudgets()
        observePendingDebts()
    }

    fun selectMonth(month: YearMonth) {
        _selectedMonth.value = month
    }

    fun addExpense(date: String, categoryId: String, amount: Double, note: String, debtId: String? = null) {
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
            userId = auth.currentUser?.uid ?: "",
            createdAt = Timestamp.now()
        )

        viewModelScope.launch {
            expenseRepository.addExpense(expense).collect { result ->
                _saveState.value = result
                if (result is Resource.Success && debtId != null) {
                    // Mark debt as SETTLED
                    debtRepository.updateDebtStatus(debtId, DebtStatus.SETTLED).collect { updateResult ->
                        // Optional: Handle update status failure if needed
                    }
                }
            }
        }
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

    private fun observeBudgets() {
        val uid = auth.currentUser?.uid ?: return
        budgetRepository.getBudgets(uid).onEach { result ->
            if (result is Resource.Success) {
                _expenseState.value = _expenseState.value.copy(
                    budgets = result.data.orEmpty()
                )
                updateNotifications()
            }
        }.launchIn(viewModelScope)
    }

    private fun updateNotifications() {
        val uid = auth.currentUser?.uid ?: return
        val state = _expenseState.value
        val today = LocalDate.now()
        val currentMonth = YearMonth.now()
        
        val notifications = mutableListOf<AppNotification>()

        // 1. Debt Notifications
        state.pendingDebts.forEach { debt ->
            debt.dueDate?.let { ts ->
                val dueDate = ts.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                val message: String
                val type: NotificationType
                
                if (dueDate.isEqual(today.plusDays(1))) {
                    message = "Khoản nợ '${debt.description}' sắp đến hạn vào ngày mai!"
                    type = NotificationType.INFO
                } else if (dueDate.isEqual(today)) {
                    message = "Khoản nợ '${debt.description}' đến hạn vào HÔM NAY!"
                    type = NotificationType.WARNING
                } else if (dueDate.isBefore(today)) {
                    message = "Khoản nợ '${debt.description}' đã QUÁ HẠN!"
                    type = NotificationType.URGENT
                } else {
                    return@forEach
                }

                notifications.add(
                    AppNotification(
                        id = debt.documentId,
                        userId = uid,
                        title = "Thông báo nợ",
                        message = message,
                        type = type,
                        amount = debt.amount,
                        relatedId = debt.documentId
                    )
                )
            }
        }

        // 2. Budget Notifications
        val categoryMap = state.categories.associateBy { it.documentId }
        state.budgets.forEach { budget ->
            val totalSpent = state.expenses
                .filter { expense ->
                    val isExpense = categoryMap[expense.categoryId]?.type == CategoryType.EXPENSE
                    if (!isExpense) return@filter false

                    val expenseDate = expense.date?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                    val isInRange = if (budget.startDate != null && budget.endDate != null) {
                        val start = budget.startDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        val end = budget.endDate.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        expenseDate != null && !expenseDate.isBefore(start) && !expenseDate.isAfter(end)
                    } else {
                        YearMonth.from(expenseDate) == currentMonth
                    }
                    
                    isInRange && (budget.categoryId.isEmpty() || expense.categoryId == budget.categoryId)
                }
                .sumOf { it.amount }

            if (totalSpent > budget.amount) {
                val categoryName = state.categories.find { it.documentId == budget.categoryId }?.name ?: "Tổng chi tiêu"
                notifications.add(
                    AppNotification(
                        id = "budget_${budget.documentId}",
                        userId = uid,
                        title = "Vượt hạn mức",
                        message = "Bạn đã chi tiêu vượt hạn mức $categoryName (${budget.amount}đ)!",
                        type = NotificationType.BUDGET_EXCEEDED,
                        amount = totalSpent - budget.amount,
                        relatedId = budget.documentId
                    )
                )
            }
        }

        // Filter out dismissed
        val filteredNotifications = notifications.filter { it.id !in _dismissedNotifications.value }
        _expenseState.value = _expenseState.value.copy(notifications = filteredNotifications)
    }

    private fun observePendingDebts() {
        val uid = auth.currentUser?.uid ?: return
        debtRepository.getDebtsByDebtor(uid).onEach { result ->
            if (result is Resource.Success) {
                _expenseState.value = _expenseState.value.copy(
                    pendingDebts = result.data?.filter { it.status == DebtStatus.PENDING }.orEmpty()
                )
                updateNotifications()
            }
        }.launchIn(viewModelScope)
    }

    private val _dismissedNotifications = MutableStateFlow<Set<String>>(emptySet())

    fun removeNotification(id: String) {
        _dismissedNotifications.value = _dismissedNotifications.value + id
        updateNotifications()
    }

    fun clearAllNotifications() {
        val allIds = _expenseState.value.notifications.map { it.id }.toSet()
        _dismissedNotifications.value = _dismissedNotifications.value + allIds
        updateNotifications()
    }

    fun setBudget(categoryId: String, amount: Long) {
        val uid = auth.currentUser?.uid ?: return
        val budget = Budget(
            userId = uid,
            categoryId = categoryId,
            amount = amount,
            name = if (categoryId.isEmpty()) "Hạn mức tổng" else "Hạn mức danh mục"
        )
        viewModelScope.launch {
            budgetRepository.addBudget(budget).collect { }
        }
    }
}
