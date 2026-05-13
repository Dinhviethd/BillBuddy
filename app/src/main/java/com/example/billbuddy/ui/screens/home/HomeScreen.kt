package com.example.billbuddy.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.ui.viewmodel.ExpenseViewModel
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
    val percentColor: Color
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
    val expenses = expenseState.expenses
    val currentMonth = remember { YearMonth.now() }
    val previousMonth = remember { currentMonth.minusMonths(1) }
    val currentMonthExpenses = remember(expenses) {
        expenses.filter { expense ->
            parseDate(expense.date)?.let { YearMonth.from(it) == currentMonth } == true
        }
    }
    val previousMonthExpenses = remember(expenses) {
        expenses.filter { expense ->
            parseDate(expense.date)?.let { YearMonth.from(it) == previousMonth } == true
        }
    }
    val totalCurrentMonth = remember(currentMonthExpenses) {
        currentMonthExpenses.sumOf { it.amount }
    }

    val categoryDefinitions = remember {
        listOf(
            CategoryUiDefinition("Ăn uống", Icons.Default.Fastfood, Color(0xFFE1BEE7)),
            CategoryUiDefinition("Giải trí", Icons.Default.Gamepad, Color(0xFFD1C4E9)),
            CategoryUiDefinition("Mua sắm", Icons.Default.ShoppingBag, Color(0xFFF8BBD0)),
            CategoryUiDefinition("Di chuyển", Icons.Default.DirectionsCar, Color(0xFFBBDEFB)),
            CategoryUiDefinition("Sức khỏe", Icons.Default.Favorite, Color(0xFFC8E6C9))
        )
    }

    val categories = remember(currentMonthExpenses, previousMonthExpenses) {
        categoryDefinitions.map { def ->
            val currentTotal = currentMonthExpenses
                .filter { it.category == def.name }
                .sumOf { it.amount }
            val previousTotal = previousMonthExpenses
                .filter { it.category == def.name }
                .sumOf { it.amount }
            val transactionCount = currentMonthExpenses.count { it.category == def.name }

            CategoryExpense(
                name = def.name,
                transactionCount = transactionCount,
                amount = formatExpenseAmount(currentTotal),
                percentage = buildPercentText(currentTotal, previousTotal),
                icon = def.icon,
                iconColor = def.iconColor,
                percentColor = percentColor(currentTotal, previousTotal)
            )
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                totalExpense = totalCurrentMonth,
                totalIncome = 0.0,
                balance = -totalCurrentMonth
            )
        },
        bottomBar = {
            HomeBottomNavigation(
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
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
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
                    TabButton(text = "Tháng trước", isSelected = false, modifier = Modifier.weight(1f))
                    TabButton(text = "Tháng này", isSelected = true, modifier = Modifier.weight(1f))
                }
            }


            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Chi tiêu tháng ${currentMonth.monthValue}/${currentMonth.year}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tổng quan chi tiêu trong tháng",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }


            items(categories) { category ->
                ExpenseCategoryCard(category)
            }

            item {
                FinancialOverviewCard(totalExpense = totalCurrentMonth)
            }

            item {
                StatisticsInfoBox()
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun HomeTopBar(
    totalExpense: Double,
    totalIncome: Double,
    balance: Double
) {
    val now = remember { YearMonth.now() }
    Surface(
        color = Color(0xFF212121),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
                Text(
                    text = "Sổ Thu Chi",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(now.year.toString(), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Thg ${now.monthValue}", style = MaterialTheme.typography.titleLarge)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                HeaderStat(label = "Chi tiêu", value = formatNumber(totalExpense))
                HeaderStat(label = "Thu nhập", value = formatNumber(totalIncome))
                HeaderStat(label = "Số dư", value = formatNumber(balance))
            }
        }
    }
}

@Composable
fun HeaderStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(40.dp),
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
                color = category.iconColor.copy(alpha = 0.5f)
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
                Text(text = category.amount, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Text(text = category.percentage, style = MaterialTheme.typography.labelSmall, color = category.percentColor)
            }
        }
    }
}

@Composable
fun FinancialOverviewCard(totalExpense: Double, startBalance: Double = 0.0) {
    val endBalance = startBalance - totalExpense
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
                "Số dư đầu tháng",
                "+${formatCurrency(startBalance)}",
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

@Composable
fun StatisticsInfoBox() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE3F2FD)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF1976D2))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text("Thống kê", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                Text(
                    "Bạn đã tiết kiệm được 42% so với mục tiêu chi tiêu tháng này",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun HomeBottomNavigation(
    onHomeClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAddClick: () -> Unit,
    onStatsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    BottomAppBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(Icons.AutoMirrored.Filled.ShowChart, "Trang chủ", true, onHomeClick)
                BottomNavItem(Icons.Default.DateRange, "Lịch", false, onCalendarClick)

                Spacer(modifier = Modifier.width(48.dp))
                
                BottomNavItem(Icons.Default.PieChart, "Thống kê", false, onStatsClick)
                BottomNavItem(Icons.Default.Person, "Cá nhân", false, onProfileClick)
            }
        }
    )
}

@Composable
fun BottomNavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val color = if (isSelected) Color(0xFFD47500) else Color.Gray
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Icon(icon, contentDescription = label, tint = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

private data class CategoryUiDefinition(
    val name: String,
    val icon: ImageVector,
    val iconColor: Color
)

private fun parseDate(value: String): LocalDate? {
    return runCatching { LocalDate.parse(value) }.getOrNull()
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

private fun formatExpenseAmount(value: Double): String {
    return if (value <= 0.0) "0đ" else "-${formatCurrency(value)}"
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

