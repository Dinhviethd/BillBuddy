package com.example.billbuddy.ui.screens.statistics

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.ui.components.AppBottomNavigation
import com.example.billbuddy.ui.theme.LightAmber
import com.example.billbuddy.ui.theme.AmberDark
import com.example.billbuddy.ui.theme.Beige
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(
    onNavigateHome: () -> Unit,
    onNavigateCalendar: () -> Unit,
    onNavigateAddExpense: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    var fromDate by remember { mutableStateOf(LocalDate.now()) }
    var toDate by remember { mutableStateOf(LocalDate.now()) }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thống kê", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.MoreVert, null)
                    }
                }
            )
        },
        bottomBar = {
            AppBottomNavigation(
                currentRoute = "statistics",
                onHomeClick = onNavigateHome,
                onCalendarClick = onNavigateCalendar,
                onStatsClick = {},
                onProfileClick = onNavigateProfile,
                onAddClick = onNavigateAddExpense
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateAddExpense,
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateRangeSection(
    fromDate: LocalDate,
    toDate: LocalDate,
    onFromDateChange: (LocalDate) -> Unit,
    onToDateChange: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Chọn khoảng thời gian",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            // From Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("TỪ", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                DatePickerField(value = "${fromDate.dayOfMonth}", onValueChange = {})
                DatePickerField(value = "${fromDate.monthValue}", onValueChange = {})
                DatePickerField(value = "${fromDate.year}", onValueChange = {})
            }

            Spacer(modifier = Modifier.height(12.dp))

            // To Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("ĐẾN", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                DatePickerField(value = "${toDate.dayOfMonth}", onValueChange = {})
                DatePickerField(value = "${toDate.monthValue}", onValueChange = {})
                DatePickerField(value = "${toDate.year}", onValueChange = {})
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("Hôm nay", fontSize = 12.sp, color = Color.Black)
                }

                Button(
                    onClick = { },
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LightAmber),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Tìm kiếm", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DatePickerField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .height(32.dp)
            .width(50.dp)
            .background(Beige, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "▼",
                fontSize = 8.sp,
                color = AmberDark
            )
        }
    }
}

@Composable
fun SummarySection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Tổng quan",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Spending
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Beige, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "2.450.000đ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Red
                        )
                        Text(
                            text = "Tổng chi tiêu",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Number of Categories
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Beige, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "8",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Danh mục",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PieChartSection() {
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

            // Sample Pie Chart
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Draw pie chart segments
                    val colors = listOf(
                        Color(0xFFED1E24), // Red
                        Color(0xFF0052CC), // Blue
                        Color(0xFF00B050), // Green
                        Color(0xFFFFD966), // Yellow
                        Color(0xFF7030A0), // Purple
                        Color(0xFFFFA500)  // Orange
                    )
                    
                    val angles = listOf(90f, 60f, 75f, 45f, 50f, 40f)
                    var startAngle = -90f
                    
                    for (i in colors.indices) {
                        drawArc(
                            color = colors[i],
                            startAngle = startAngle,
                            sweepAngle = angles[i],
                            useCenter = true
                        )
                        startAngle += angles[i]
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Chưa có dữ liệu",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CategoryDetailsSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Chi tiết danh mục",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Sample category items
            val categories = listOf(
                CategoryItem("Ăn uống", 850000, Color.Red),
                CategoryItem("Số thuyền", 450000, Color.Blue),
                CategoryItem("Mua sắm", 380000, Color.Green),
                CategoryItem("Gía sứ", 320000, Color(0xFFFFD966)),
                CategoryItem("Y tế", 280000, Color(0xFF7030A0)),
                CategoryItem("Khác", 200000, Color(0xFFFFA500))
            )

            categories.forEach { category ->
                CategoryDetailItem(category = category)
                if (category != categories.last()) {
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Chưa có dữ liệu",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )
        }
    }
}

@Composable
fun CategoryDetailItem(category: CategoryItem) {
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
                    .background(category.color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                fontSize = 12.sp,
                color = Color.Black
            )
        }

        Text(
            text = "${category.amount}đ",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )
    }
}

data class CategoryItem(
    val name: String,
    val amount: Int,
    val color: Color
)
