package com.example.billbuddy.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.billbuddy.ui.components.AppBottomNavigation
import com.example.billbuddy.ui.viewmodel.ExpenseViewModel
import com.google.firebase.Timestamp
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs
import com.example.billbuddy.navigation.Screen
import com.example.billbuddy.ui.components.AppBottomNavigation
import com.example.billbuddy.ui.components.StatisticsInfoBox
import com.example.billbuddy.ui.viewmodel.AuthViewModel

data class CategoryExpense(
    val name: String,
    val transactionCount: Int,
    val amount: String,
    val percentage: String,
    val icon: ImageVector,
    val iconColor: Color,
    val percentColor: Color
)

@RequiresApi(Build.VERSION_CODES.O)
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
    
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val previousMonth = remember(selectedMonth) { selectedMonth.minusMonths(1) }
    
    val currentMonthExpenses = remember(expenses, selectedMonth) {
        expenses.filter { expense ->
            parseDate(expense.date)?.let { YearMonth.from(it) == selectedMonth } == true
        }
    }
    val previousMonthExpenses = remember(expenses, selectedMonth) {
        expenses.filter { expense ->
            parseDate(expense.date)?.let { YearMonth.from(it) == previousMonth } == true
        }
    }

    val totalCurrentMonthExpense = remember(currentMonthExpenses) {
        currentMonthExpenses.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    }
    val totalCurrentMonthIncome = remember(currentMonthExpenses) {
        currentMonthExpenses.filter { it.type == "INCOME" }.sumOf { it.amount }
    }
    val balance = totalCurrentMonthIncome - totalCurrentMonthExpense

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
                .filter { it.categoryId == def.name && it.type == "EXPENSE" }
                .sumOf { it.amount }
            val previousTotal = previousMonthExpenses
                .filter { it.categoryId == def.name && it.type == "EXPENSE" }
                .sumOf { it.amount }
            val transactionCount = currentMonthExpenses.count { it.categoryId == def.name && it.type == "EXPENSE" }

            CategoryExpense(
                name = def.name,
                transactionCount = transactionCount,
                amount = formatExpenseAmount(currentTotal.toDouble()),
                percentage = buildPercentText(currentTotal.toDouble(), previousTotal.toDouble()),
                icon = def.icon,
                iconColor = def.iconColor,
                percentColor = percentColor(currentTotal.toDouble(), previousTotal.toDouble())
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Tổng Quan Thu Chi",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        bottomBar = {
            AppBottomNavigation(
                currentRoute = Screen.Home.route,
                onHomeClick = {},
                onCalendarClick = onNavigateToCalendar,
                onStatsClick = onNavigateToStatistics,
                onProfileClick = onNavigateToProfile,
                onAddClick = onNavigateToAddExpense
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
                    TabButton(
                        text = "Tháng ${previousMonth.monthValue}",
                        isSelected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMonth = previousMonth }
                    )
                    TabButton(
                        text = "Tháng ${selectedMonth.monthValue}",
                        isSelected = true,
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    )
                    TabButton(
                        text = "Tháng ${(selectedMonth.plusMonths(1)).monthValue}",
                        isSelected = false,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedMonth = selectedMonth.plusMonths(1) }
                    )
                }
            }

            item {
                HomeSummaryHeader(
                    totalExpense = totalCurrentMonthExpense.toDouble(),
                    totalIncome = totalCurrentMonthIncome.toDouble(),
                    balance = balance.toDouble()
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


            items(categories) { category ->
                ExpenseCategoryCard(category)
            }

            item {
                FinancialOverviewCard(
                    totalExpense = totalCurrentMonthExpense.toDouble(),
                    totalIncome = totalCurrentMonthIncome.toDouble()
                )
            }

//            item {
//                StatisticsInfoBox()
//            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun HomeSummaryHeader(
    totalExpense: Double,
    totalIncome: Double,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Số dư hiện tại", color = Color.Gray, fontSize = 14.sp)
            Text(
                formatCurrency(balance),
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Thu nhập", color = Color.Gray, fontSize = 12.sp)
                    Text(formatNumber(totalIncome), color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                }
                VerticalDivider(modifier = Modifier.height(30.dp), color = Color.Gray)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Chi tiêu", color = Color.Gray, fontSize = 12.sp)
                    Text(formatNumber(totalExpense), color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
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
fun FinancialOverviewCard(totalExpense: Double, totalIncome: Double) {
    val endBalance = totalIncome - totalExpense
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
                "Tổng thu nhập",
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

private data class CategoryUiDefinition(
    val name: String,
    val icon: ImageVector,
    val iconColor: Color
)

private fun parseDate(timestamp: Timestamp?): LocalDate? {
    return timestamp?.toDate()?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
}

private fun formatNumber(value: Double): String {
    val formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
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
