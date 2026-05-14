package com.example.billbuddy.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.billbuddy.data.model.Expense
import com.example.billbuddy.ui.screens.home.BottomNavItem
import com.example.billbuddy.ui.theme.AppBackground
import com.example.billbuddy.ui.theme.LightAmber
import com.example.billbuddy.ui.viewmodel.CalendarViewModel
import com.example.billbuddy.utils.Resource
import java.util.Locale

fun getCategoryIcon(category: String): ImageVector {
    return when (category) {
        "Ăn uống" -> Icons.Default.Restaurant
        "Giải trí" -> Icons.Default.VideogameAsset
        "Mua sắm" -> Icons.Default.ShoppingBag
        "Di chuyển" -> Icons.Default.DirectionsCar
        "Sức khỏe" -> Icons.Default.Favorite
        else -> Icons.Default.Category
    }
}

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Ăn uống" -> Color(0xFFFFE0B2)
        "Giải trí" -> Color(0xFFE9D5FF)
        "Mua sắm" -> Color(0xFFFBCFE8)
        "Di chuyển" -> Color(0xFFBFDBFE)
        "Sức khỏe" -> Color(0xFFC7F9CC)
        else -> Color(0xFFF3F4F6)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateHome: () -> Unit,
    onNavigateAddExpense: () -> Unit,
    onNavigateStatistics: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    
    var showEditDialog by remember { mutableStateOf<Expense?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(editState) {
        if (editState is Resource.Success) {
            snackbarHostState.showSnackbar("Đã cập nhật chi tiêu")
            viewModel.clearEditState()
        } else if (editState is Resource.Error) {
            snackbarHostState.showSnackbar((editState as Resource.Error).message ?: "Lỗi khi cập nhật")
            viewModel.clearEditState()
        }
    }

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
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
            BottomAppBar(
                containerColor = Color.White
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BottomNavItem(
                        icon = Icons.AutoMirrored.Filled.ShowChart,
                        label = "Trang chủ",
                        isSelected = false,
                        onClick = onNavigateHome
                    )

                    BottomNavItem(
                        icon = Icons.Default.DateRange,
                        label = "Lịch",
                        isSelected = true,
                        onClick = {}
                    )

                    Spacer(modifier = Modifier.width(48.dp))

                    BottomNavItem(
                        icon = Icons.Default.PieChart,
                        label = "Thống kê",
                        isSelected = false,
                        onClick = onNavigateStatistics
                    )

                    BottomNavItem(
                        icon = Icons.Default.Person,
                        label = "Cá nhân",
                        isSelected = false,
                        onClick = onNavigateProfile
                    )
                }
            }
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
                    selectedDay = uiState.selectedDay,
                    selectedMonth = uiState.selectedMonth,
                    selectedYear = uiState.selectedYear,
                    onFilterClick = { d, m, y ->
                        viewModel.setDateFilter(d, m, y)
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
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (uiState.filteredExpenses.isEmpty()) {
                item {
                    Text("Không có chi tiêu nào trong ngày này", modifier = Modifier.padding(16.dp))
                }
            } else {
                items(uiState.filteredExpenses.size) { index ->
                    val expense = uiState.filteredExpenses[index]
                    ExpenseItem(
                        expense = expense,
                        time = viewModel.formatTime(expense.createdAt),
                        onClick = { showEditDialog = expense }
                    )
                }
            }

            item {
                TotalExpenseCard(String.format(Locale.getDefault(), "-%,.0fđ", uiState.totalAmount))
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    if (showEditDialog != null) {
        EditExpenseDialog(
            expense = showEditDialog!!,
            onDismiss = { showEditDialog = null },
            onConfirm = { updatedExpense ->
                viewModel.updateExpense(updatedExpense)
                showEditDialog = null
            },
            onDelete = {
                viewModel.deleteExpense(showEditDialog!!.id)
                showEditDialog = null
            }
        )
    }
}

@Composable
fun DateFilterCard(
    selectedDay: String,
    selectedMonth: String,
    selectedYear: String,
    onFilterClick: (String, String, String) -> Unit
) {
    var day by remember(selectedDay) { mutableStateOf(selectedDay) }
    var month by remember(selectedMonth) { mutableStateOf(selectedMonth) }
    var year by remember(selectedYear) { mutableStateOf(selectedYear) }

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
                    onValueChange = { day = it }
                )

                DateDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Tháng",
                    value = month,
                    options = (1..12).map { String.format(Locale.getDefault(), "%02d", it) },
                    onValueChange = { month = it }
                )

                DateDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Năm",
                    value = year,
                    options = (2020..2030).map { it.toString() },
                    onValueChange = { year = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onFilterClick(day, month, year) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LightAmber
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hiển thị")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateDropdown(
    modifier: Modifier,
    label: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedCard(
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(value)
                    Icon(
                        Icons.Default.ArrowDropDown,
                        null
                    )
                }
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            onValueChange(selectionOption)
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
    expense: Expense,
    time: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.clickable { onClick() }
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
                    .background(
                        getCategoryColor(expense.category),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getCategoryIcon(expense.category),
                    null,
                    tint = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    expense.category,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    time,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                String.format(Locale.getDefault(), "-%,.0fđ", expense.amount),
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TotalExpenseCard(total: String) {
    Card(
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Tổng chi tiêu",
                fontWeight = FontWeight.SemiBold
            )

            Text(
                total,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun EditExpenseDialog(
    expense: Expense,
    onDismiss: () -> Unit,
    onConfirm: (Expense) -> Unit,
    onDelete: () -> Unit
) {
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var note by remember { mutableStateOf(expense.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Chỉnh sửa chi tiêu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Danh mục: ${expense.category}", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Số tiền") }
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú") }
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = expense.copy(
                    amount = amount.toDoubleOrNull() ?: expense.amount,
                    note = note
                )
                onConfirm(updated)
            }) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDelete) {
                Text("Xóa", color = Color.Red)
            }
        }
    )
}
