package com.example.billbuddy.ui.screens.calendar

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billbuddy.data.model.AppNotification
import com.example.billbuddy.data.model.CategoryType
import com.example.billbuddy.navigation.Screen
import com.example.billbuddy.ui.components.AppBottomNavigation
import com.example.billbuddy.ui.components.NotificationIconButton
import com.example.billbuddy.ui.theme.AppBackground
import com.example.billbuddy.ui.theme.LightAmber
import com.example.billbuddy.ui.viewmodel.CalendarViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateHome: () -> Unit,
    onNavigateAddExpense: () -> Unit,
    onNavigateStatistics: () -> Unit,
    onNavigateProfile: () -> Unit,
    notifications: List<AppNotification> = emptyList(),
    onRemoveNotification: (String) -> Unit = {},
    onClearAll: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val categoryMap = remember(uiState.categories) { uiState.categories.associateBy { it.documentId } }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Lịch Chi Tiêu",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    NotificationIconButton(
                        notifications = notifications,
                        onRemoveNotification = onRemoveNotification,
                        onClearAll = onClearAll
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateAddExpense,
                containerColor = Color(0xFFD47500),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.offset(y = 50.dp)
            ) {
                Icon(Icons.Default.Add, null)
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            AppBottomNavigation(
                currentRoute = Screen.Calendar.route,
                onHomeClick = onNavigateHome,
                onCalendarClick = {},
                onAddClick = onNavigateAddExpense,
                onStatsClick = onNavigateStatistics,
                onProfileClick = onNavigateProfile
            )
        },
        containerColor = AppBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DateFilterCard(
                    initialDay = uiState.selectedDay,
                    initialMonth = uiState.selectedMonth,
                    initialYear = uiState.selectedYear,
                    onShowClick = { day, month, year ->
                        viewModel.setDateFilter(day, month, year)
                    }
                )
            }

            item {
                Text(
                    "Chi tiêu ngày ${uiState.selectedDay}/${uiState.selectedMonth}/${uiState.selectedYear}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(30.dp))
                    }
                }
            } else if (uiState.filteredExpenses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Không có giao dịch nào trong ngày này", color = Color.Gray)
                    }
                }
            } else {
                items(uiState.filteredExpenses) { expense ->
                    val category = categoryMap[expense.categoryId]
                    val icon = when (category?.icon) {
                        "restaurant" -> Icons.Default.Restaurant
                        "directions_car" -> Icons.Default.DirectionsCar
                        "shopping_bag" -> Icons.Default.ShoppingBag
                        "payments" -> Icons.Default.Payments
                        else -> Icons.Default.Category
                    }
                    val iconBg = try {
                        Color(android.graphics.Color.parseColor(category?.color ?: "#EEEEEE"))
                    } catch (_: Exception) {
                        Color.LightGray
                    }

                    ExpenseItem(
                        title = category?.name ?: "Khác",
                        time = viewModel.formatTime(expense.date),
                        amount = expense.amount,
                        icon = icon,
                        iconBg = iconBg.copy(alpha = 0.2f),
                        iconTint = iconBg,
                        type = category?.type ?: CategoryType.EXPENSE
                    )
                }

                item {
                    TotalExpenseCard(uiState.totalDayAmount)
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DateFilterCard(
    initialDay: String,
    initialMonth: String,
    initialYear: String,
    onShowClick: (String, String, String) -> Unit
) {
    var day by remember(initialDay) { mutableStateOf(initialDay) }
    var month by remember(initialMonth) { mutableStateOf(initialMonth) }
    var year by remember(initialYear) { mutableStateOf(initialYear) }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF7E7)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DateDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Ngày",
                    value = day,
                    options = (1..31).map { String.format(Locale.getDefault(), "%02d", it) },
                    onSelect = { day = it }
                )

                DateDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Tháng",
                    value = month,
                    options = (1..12).map { String.format(Locale.getDefault(), "%02d", it) },
                    onSelect = { month = it }
                )

                DateDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Năm",
                    value = year,
                    options = (2020..2030).map { it.toString() },
                    onSelect = { year = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onShowClick(day, month, year) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightAmber
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hiển thị", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DateDropdown(
    modifier: Modifier,
    label: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(6.dp))

        Box {
            OutlinedCard(
                modifier = Modifier.clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(value, fontSize = 14.sp)
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItem(
    title: String,
    time: String,
    amount: Long,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    type: CategoryType
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )

                Text(
                    time,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            val amountText = if (type == CategoryType.INCOME) "+${formatMoney(amount.toDouble())}" else "-${formatMoney(amount.toDouble())}"
            val amountColor = if (type == CategoryType.INCOME) Color(0xFF4CAF50) else Color(0xFFE53935)

            Text(
                amountText,
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun TotalExpenseCard(total: Double) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Tổng trong ngày",
                fontWeight = FontWeight.Bold
            )

            val totalColor = if (total >= 0) Color(0xFF1976D2) else Color(0xFFE53935)
            val totalPrefix = if (total > 0) "+" else ""

            Text(
                "$totalPrefix${formatMoney(total)}",
                color = totalColor,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

private fun formatMoney(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    return formatter.format(amount).replace("₫", "đ")
}
