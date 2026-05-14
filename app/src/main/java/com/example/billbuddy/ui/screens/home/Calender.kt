package com.example.billbuddy.ui.screens.calendar

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.ui.screens.home.BottomNavItem
import com.example.billbuddy.ui.theme.AppBackground
import com.example.billbuddy.ui.theme.LightAmber

data class DailyExpense(
    val title: String,
    val time: String,
    val amount: String,
    val icon: ImageVector,
    val iconBg: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onNavigateHome: () -> Unit,
    onNavigateAddExpense: () -> Unit,
    onNavigateStatistics: () -> Unit,
    onNavigateProfile: () -> Unit
) {

    val expenses = listOf(
        DailyExpense(
            "Ăn uống",
            "12:30 PM",
            "-250,000đ",
            Icons.Default.Restaurant,
            Color(0xFFFFE0B2)
        ),
        DailyExpense(
            "Giải trí",
            "7:00 PM",
            "-180,000đ",
            Icons.Default.VideogameAsset,
            Color(0xFFE9D5FF)
        ),
        DailyExpense(
            "Mua sắm",
            "3:15 PM",
            "-450,000đ",
            Icons.Default.ShoppingBag,
            Color(0xFFFBCFE8)
        ),
        DailyExpense(
            "Di chuyển",
            "8:00 AM",
            "-50,000đ",
            Icons.Default.DirectionsCar,
            Color(0xFFBFDBFE)
        ),
        DailyExpense(
            "Sức khỏe",
            "10:45 AM",
            "-120,000đ",
            Icons.Default.Favorite,
            Color(0xFFC7F9CC)
        )
    )

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
                DateFilterCard()
            }

            item {

                Text(
                    "Chi tiêu ngày 15/04/2025",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            items(expenses.size) { index ->

                ExpenseItem(expenses[index])
            }

            item {

                TotalExpenseCard("-1,050,000đ")
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DateFilterCard() {

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
                    value = "15"
                )

                DateDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Tháng",
                    value = "04"
                )

                DateDropdown(
                    modifier = Modifier.weight(1f),
                    label = "Năm",
                    value = "2025"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {},
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

@Composable
fun DateDropdown(
    modifier: Modifier,
    label: String,
    value: String
) {

    Column(modifier = modifier) {

        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )

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

                Icon(
                    Icons.Default.ArrowDropDown,
                    null
                )
            }
        }
    }
}

@Composable
fun ExpenseItem(expense: DailyExpense) {

    Card(
        shape = RoundedCornerShape(18.dp)
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
                        expense.iconBg,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    expense.icon,
                    null,
                    tint = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    expense.title,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    expense.time,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Text(
                expense.amount,
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