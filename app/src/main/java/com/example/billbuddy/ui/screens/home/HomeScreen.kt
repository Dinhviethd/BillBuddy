package com.example.billbuddy.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.data.model.AppNotification
import com.example.billbuddy.data.model.Budget
import com.example.billbuddy.data.model.Category
import com.example.billbuddy.data.model.CategoryType
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.navigation.Screen
import com.example.billbuddy.ui.components.AppBottomNavigation
import com.example.billbuddy.ui.components.NotificationIconButton
import com.example.billbuddy.ui.viewmodel.ExpenseViewModel
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale
import kotlin.math.abs

data class CategoryExpense(
    val name: String,
    val transactionCount: Int,
    val amount: String,
    val percentage: String,
    val icon: ImageVector,
    val iconColor: Color,
    val percentColor: Color,
    val type: CategoryType
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ExpenseViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val expenseState by viewModel.expenseState.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val expenses = expenseState.expenses
    val categoriesFromDb = expenseState.categories

    val currentMonthExpenses = remember(expenses, selectedMonth) {
        expenses.filter { expense ->
            parseDate(expense.date)?.let { YearMonth.from(it) == selectedMonth } == true
        }
    }
    
    val previousMonth = selectedMonth.minusMonths(1)
    val previousMonthExpenses = remember(expenses, previousMonth) {
        expenses.filter { expense ->
            parseDate(expense.date)?.let { YearMonth.from(it) == previousMonth } == true
        }
    }
    
    val categoryMap = remember(categoriesFromDb) {
        categoriesFromDb.associateBy { it.documentId }
    }

    val totalCurrentMonthIncome = remember(currentMonthExpenses, categoryMap) {
        currentMonthExpenses
            .filter { categoryMap[it.categoryId]?.type == CategoryType.INCOME }
            .sumOf { it.amount }
    }

    val totalCurrentMonthExpense = remember(currentMonthExpenses, categoryMap) {
        currentMonthExpenses
            .filter { categoryMap[it.categoryId]?.type == CategoryType.EXPENSE }
            .sumOf { it.amount }
    }

    val mappedCategories = remember(currentMonthExpenses, previousMonthExpenses, categoriesFromDb) {
        categoriesFromDb.map { category ->
            val currentTotal = currentMonthExpenses
                .filter { it.categoryId == category.documentId }
                .sumOf { it.amount }
            val previousTotal = previousMonthExpenses
                .filter { it.categoryId == category.documentId }
                .sumOf { it.amount }
            val transactionCount = currentMonthExpenses.count { it.categoryId == category.documentId }

            CategoryExpense(
                name = category.name,
                transactionCount = transactionCount,
                amount = formatAmountByType(currentTotal.toDouble(), category.type),
                percentage = buildPercentText(currentTotal.toDouble(), previousTotal.toDouble()),
                icon = when(category.icon) {
                    "restaurant" -> Icons.Default.Restaurant
                    "directions_car" -> Icons.Default.DirectionsCar
                    "shopping_bag" -> Icons.Default.ShoppingBag
                    "payments" -> Icons.Default.Payments
                    else -> Icons.Default.Category
                },
                iconColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch(_: Exception) { Color.Gray },
                percentColor = percentColor(currentTotal.toDouble(), previousTotal.toDouble()),
                type = category.type
            )
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                notifications = expenseState.notifications
            )
        },
        bottomBar = {
            AppBottomNavigation(
                currentRoute = Screen.Home.route,
                onHomeClick = {},
                onCalendarClick = onNavigateToCalendar,
                onAddClick = onNavigateToAddExpense,
                onStatsClick = onNavigateToStatistics,
                onProfileClick = onNavigateToProfile
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddExpense,
                shape = CircleShape,
                containerColor = Color(0xFFD47500),
                contentColor = Color.White,
                modifier = Modifier.offset(y = 50.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (expenseState.isLoading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            expenseState.errorMessage?.let { message ->
                item {
                    Text(text = message, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TabButton(
                        text = "Tháng trước", 
                        isSelected = false, 
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectMonth(selectedMonth.minusMonths(1)) }
                    )
                    TabButton(
                        text = "Tháng này", 
                        isSelected = true, 
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectMonth(YearMonth.now()) }
                    )
                }
            }

            item {
                BudgetSection(
                    budgets = expenseState.budgets,
                    expenses = expenses,
                    categories = categoriesFromDb,
                    selectedMonth = selectedMonth
                )
            }

            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Chi tiêu tháng ${selectedMonth.monthValue}/${selectedMonth.year}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tổng quan chi tiêu trong tháng",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }


            items(mappedCategories) { category ->
                ExpenseCategoryCard(category)
            }

            item {
                FinancialOverviewCard(
                    totalExpense = totalCurrentMonthExpense.toDouble(),
                    totalIncome = totalCurrentMonthIncome.toDouble()
                )
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun BudgetSection(
    budgets: List<Budget>,
    expenses: List<Expense>,
    categories: List<Category>,
    selectedMonth: YearMonth
) {
    if (budgets.isEmpty()) return

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = "Hạn mức chi tiêu",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        budgets.forEach { budget ->
            BudgetCard(budget, expenses, categories, selectedMonth)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun BudgetCard(
    budget: Budget,
    expenses: List<Expense>,
    categories: List<Category>,
    selectedMonth: YearMonth
) {
    val category = categories.find { it.documentId == budget.categoryId }
    val spent = expenses.filter { expense ->
        val expenseDate = parseDate(expense.date)
        val isInMonth = expenseDate?.let { YearMonth.from(it) == selectedMonth } ?: false
        val isCorrectCategory = budget.categoryId.isEmpty() || expense.categoryId == budget.categoryId
        val isExpense = categories.find { it.documentId == expense.categoryId }?.type == CategoryType.EXPENSE
        isInMonth && isCorrectCategory && isExpense
    }.sumOf { it.amount }

    val progress = if (budget.amount > 0) (spent.toFloat() / budget.amount) else 0f
    val color = if (progress > 1f) Color.Red else Color(0xFFE8B931)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (budget.categoryId.isEmpty()) "Tổng chi tiêu" else (category?.name ?: "Danh mục"),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "${formatCurrency(spent.toDouble())} / ${formatCurrency(budget.amount.toDouble())}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
            if (progress > 1f) {
                Text(
                    text = "Đã vượt hạn mức!",
                    color = Color.Red,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    notifications: List<AppNotification>
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Trang chủ",
                fontWeight = FontWeight.Bold
            )
        },
        actions = {
            NotificationIconButton(
                notifications = notifications
            )
        }
    )
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Surface(
        modifier = modifier.height(40.dp).clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) Color(0xFFE8B931) else Color(0xFFF5F5F5)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun ExpenseCategoryCard(category: CategoryExpense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = category.iconColor.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp),
                    tint = category.iconColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = category.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium))
                Text(text = "${category.transactionCount} giao dịch", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                val amountColor = if (category.type == CategoryType.INCOME) Color(0xFF4CAF50) else Color.Black
                Text(
                    text = category.amount, 
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = amountColor
                )
                Text(text = category.percentage, style = MaterialTheme.typography.labelSmall, color = category.percentColor)
            }
        }
    }
}

@Composable
fun FinancialOverviewCard(totalExpense: Double, totalIncome: Double, startBalance: Double = 0.0) {
    val endBalance = startBalance + totalIncome - totalExpense
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDE7))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tổng quan tài chính",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OverviewRow(
                Icons.Default.ArrowUpward,
                "Thu nhập",
                "+${formatCurrency(totalIncome)}",
                Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(12.dp))
            OverviewRow(
                Icons.Default.ArrowDownward,
                "Tổng chi tiêu",
                "-${formatCurrency(totalExpense)}",
                Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFD54F).copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF1976D2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Số dư cuối tháng", fontWeight = FontWeight.Medium)
                    }
                    Text(
                        formatCurrency(endBalance),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OverviewRow(icon: ImageVector, label: String, amount: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = color.copy(alpha = 0.1f), modifier = Modifier.size(24.dp)) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(4.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Color.Gray)
        }
        Text(amount, color = color, fontWeight = FontWeight.Medium)
    }
}

private fun parseDate(timestamp: Timestamp?): LocalDate? {
    return timestamp?.let {
        java.time.Instant.ofEpochMilli(it.toDate().time)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }
}

private fun formatNumber(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return formatter.format(value)
}

private fun formatCurrency(value: Double): String {
    return "${formatNumber(abs(value))}đ"
}

private fun formatAmountByType(value: Double, type: CategoryType): String {
    return if (type == CategoryType.INCOME) {
        if (value <= 0.0) "0đ" else "+${formatCurrency(value)}"
    } else {
        if (value <= 0.0) "0đ" else "-${formatCurrency(value)}"
    }
}

private fun buildPercentText(current: Double, previous: Double): String {
    if (previous <= 0.0) {
        return if (current <= 0.0) "Không đổi" else "+100% so với tháng trước"
    }
    val change = ((current - previous) / previous) * 100
    val sign = if (change >= 0) "+" else ""
    val percent = String.format(Locale.US, "%.0f", change)
    return "$sign$percent% so với tháng trước"
}

private fun percentColor(current: Double, previous: Double): Color {
    if (previous <= 0.0) {
        return if (current <= 0.0) Color.Gray else Color(0xFFF44336)
    }
    return if (current <= previous) Color(0xFF4CAF50) else Color(0xFFF44336)
}
