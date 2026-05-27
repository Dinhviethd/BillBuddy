package com.example.billbuddy.ui.screens.statistics
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.ui.components.AppBottomNavigation
import com.example.billbuddy.ui.theme.LightAmber
import com.example.billbuddy.navigation.Screen
import com.example.billbuddy.ui.theme.AmberDark
import com.example.billbuddy.ui.theme.Beige
import com.example.billbuddy.ui.theme.LightAmber
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var fromDate by remember { mutableStateOf(LocalDate.now()) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }

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

            // Date Range Selection
            DateRangeSection(
                fromDate = fromDate,
                toDate = toDate,
                onFromDateChange = { fromDate = it },
                onToDateChange = { toDate = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Section
            SummarySection()

            Spacer(modifier = Modifier.height(16.dp))

            // Pie Chart Section
            PieChartSection()

            Spacer(modifier = Modifier.height(16.dp))

            // Category Details Section
            CategoryDetailsSection()

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
@Composable
private fun Header() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(LightAmber)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Thống kê",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun DateRangeSection(
    fromDate: LocalDate,
    toDate: LocalDate,
    onTodayClick: () -> Unit,
    onRefreshClick: () -> Unit
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
                    modifier = Modifier.weight(1f),
                    label = "Từ",
                    value = fromDate.format(dateFormatter)
                )
                DateBadge(
                    modifier = Modifier.weight(1f),
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Hôm nay", fontSize = 12.sp, color = Color.Black)
                }
                Button(
                    onClick = onRefreshClick,
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
    value: String
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
                Text(value)
                Icon(Icons.Default.DateRange, contentDescription = null, tint = AmberDark)
            }
        }
    }
}
@Composable
private fun SummarySection(
    totalAmount: Double,
    categoryCount: Int,
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
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    value = formatMoney(totalAmount),
                    label = "Tổng chi tiêu",
                    valueColor = Color.Red
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    value = categoryCount.toString(),
                    label = "Danh mục",
                    valueColor = Color.Black
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
private fun PieChartSection(categories: List<com.example.billbuddy.ui.viewmodel.CategorySummary>) {
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
            val chartColors = categories.mapIndexed { index, _ ->
                pieChartColors[index % pieChartColors.size]
            }
            val total = categories.sumOf { it.amount }
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (total > 0.0) {
                        var startAngle = -90f
                        categories.forEachIndexed { index, category ->
                            val sweepAngle = ((category.amount / total) * 360f).toFloat()
                            drawArc(
                                color = chartColors[index],
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true
                            )
                            startAngle += sweepAngle
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Đã có dữ liệu thống kê",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
private fun CategoryDetailsSection(categories: List<com.example.billbuddy.ui.viewmodel.CategorySummary>) {
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
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }
}
@Composable
private fun CategoryDetailItem(
    category: com.example.billbuddy.ui.viewmodel.CategorySummary,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                fontSize = 12.sp,
                color = Color.Black
            )
        }
        Text(
            text = formatMoney(category.amount),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
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
                .padding(20.dp),
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
                .padding(24.dp),
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}
private fun formatMoney(amount: Double): String {
    return String.format(Locale("vi", "VN"), "%,.0fđ", amount)
}
