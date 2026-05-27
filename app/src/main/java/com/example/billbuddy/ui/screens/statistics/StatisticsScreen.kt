package com.example.billbuddy.ui.screens.statistics

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.billbuddy.navigation.Screen
import com.example.billbuddy.ui.components.AppBottomNavigation
import com.example.billbuddy.ui.theme.AmberDark
import com.example.billbuddy.ui.theme.Beige
import com.example.billbuddy.ui.theme.LightAmber
import com.example.billbuddy.ui.viewmodel.CategorySummary
import com.example.billbuddy.ui.viewmodel.StatisticsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val pieChartColors = listOf(
    Color(0xFFFF7043),
    Color(0xFF42A5F5),
    Color(0xFFAB47BC),
    Color(0xFF66BB6A),
    Color(0xFFFFA726),
    Color(0xFF26A69A),
    Color(0xFFEC407A),
    Color(0xFF7E57C2),
    Color(0xFF9CCC65),
    Color(0xFF26C6DA)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val fromDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            viewModel.setFromDate(LocalDate.of(year, month + 1, dayOfMonth))
        },
        uiState.fromDate.year,
        uiState.fromDate.monthValue - 1,
        uiState.fromDate.dayOfMonth
    )

    val toDatePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            viewModel.setToDate(LocalDate.of(year, month + 1, dayOfMonth))
        },
        uiState.toDate.year,
        uiState.toDate.monthValue - 1,
        uiState.toDate.dayOfMonth
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Thống kê",
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
                currentRoute = Screen.Statistics.route,
                onHomeClick = onNavigateToHome,
                onCalendarClick = onNavigateToCalendar,
                onAddClick = onNavigateToAddExpense,
                onStatsClick = {},
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
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFFF9EB))
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            DateRangeSection(
                fromDate = uiState.fromDate,
                toDate = uiState.toDate,
                onFromClick = { fromDatePickerDialog.show() },
                onToClick = { toDatePickerDialog.show() },
                onTodayClick = { viewModel.setTodayRange() }
            ) { viewModel.refresh() }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                LoadingCard()
            } else if (uiState.errorMessage != null) {
                EmptyStateCard(text = uiState.errorMessage!!)
            } else if (!uiState.hasData) {
                EmptyStateCard(text = "Không có dữ liệu chi tiêu trong khoảng thời gian này.")
            } else {
                SummarySection(
                    totalIncome = uiState.totalIncome,
                    totalExpense = uiState.totalExpense,
                    balance = uiState.balance,
                    expenseCount = uiState.expenseCount
                )

                Spacer(modifier = Modifier.height(16.dp))

                PieChartSection(uiState.categorySummaries)

                Spacer(modifier = Modifier.height(16.dp))

                CategoryDetailsSection(uiState.categorySummaries)
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateRangeSection(
    fromDate: LocalDate,
    toDate: LocalDate,
    onFromClick: () -> Unit,
    onToClick: () -> Unit,
    onTodayClick: () -> Unit,
    onSearchClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chọn khoảng thời gian",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DateBadge(
                    modifier = Modifier.weight(1f).clickable { onFromClick() },
                    label = "Từ",
                    value = fromDate.format(dateFormatter)
                )
                DateBadge(
                    modifier = Modifier.weight(1f).clickable { onToClick() },
                    label = "Đến",
                    value = toDate.format(dateFormatter)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onTodayClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hôm nay", fontSize = 12.sp, color = Color.Black)
                }
                Button(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Tìm kiếm",
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun DateBadge(
    modifier: Modifier,
    label: String,
    value: String,
) {
    Column(modifier = modifier) {
        Text(label, fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedCard {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, fontSize = 13.sp)
                Icon(Icons.Default.DateRange, contentDescription = null, tint = AmberDark, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun SummarySection(
    totalIncome: Double,
    totalExpense: Double,
    balance: Double,
    expenseCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Tổng quan",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    value = formatMoney(totalIncome),
                    label = "Thu nhập",
                    valueColor = Color(0xFF4CAF50)
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    value = formatMoney(totalExpense),
                    label = "Chi tiêu",
                    valueColor = Color(0xFFE53935)
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    value = formatMoney(balance),
                    label = "Số dư",
                    valueColor = if (balance >= 0) Color(0xFF1976D2) else Color(0xFFE53935)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Số giao dịch: $expenseCount",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun StatBox(
    modifier: Modifier,
    value: String,
    label: String,
    valueColor: Color
) {
    Box(
        modifier = modifier
            .background(Beige, RoundedCornerShape(8.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                textAlign = TextAlign.Center
            )
            Text(text = label, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun PieChartSection(categories: List<CategorySummary>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chỉ tiêu theo danh mục",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            val total = categories.sumOf { it.amount }

            Box(
                modifier = Modifier
                    .size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (total > 0.0) {
                        var startAngle = -90f
                        categories.forEachIndexed { index, category ->
                            val sweepAngle = ((category.amount / total) * 360f).toFloat()
                            drawArc(
                                color = pieChartColors[index % pieChartColors.size],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                    } else {
                        drawCircle(color = Color.LightGray, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 20f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Cơ cấu chi tiêu (%)",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryDetailsSection(categories: List<CategorySummary>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chi tiết danh mục",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            categories.forEachIndexed { index, category ->
                CategoryDetailItem(
                    category = category,
                    color = pieChartColors[index % pieChartColors.size]
                )
                if (index != categories.lastIndex) {
                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun CategoryDetailItem(
    category: CategorySummary,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = category.name,
                fontSize = 13.sp,
                color = Color.Black
            )
        }
        Text(
            text = formatMoney(category.amount),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.size(12.dp))
            Text(text = "Đang tải dữ liệu...", fontSize = 14.sp)
        }
    }
}

@Composable
private fun EmptyStateCard(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatMoney(amount: Double): String {
    val formatter = java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    return formatter.format(amount).replace("₫", "đ")
}
