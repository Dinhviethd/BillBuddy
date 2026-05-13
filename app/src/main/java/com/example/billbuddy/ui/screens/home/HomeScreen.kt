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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    onNavigateToAddExpense: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Sổ Thu Chi",
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
            item {
                HomeStatsHeader()
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
                        text = "Chi tiêu tháng 12/2024",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Tổng quan chi tiêu trong tháng",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }


            val categories = listOf(
                CategoryExpense("Ăn uống", 8, "-1,200,000đ", "-5% so với tháng trước", Icons.Default.Fastfood, Color(0xFFE1BEE7), Color(0xFF4CAF50)),
                CategoryExpense("Giải trí", 8, "-1,200,000đ", "-5% so với tháng trước", Icons.Default.Gamepad, Color(0xFFD1C4E9), Color(0xFF4CAF50)),
                CategoryExpense("Mua sắm", 12, "-3,800,000đ", "+25% so với tháng trước", Icons.Default.ShoppingBag, Color(0xFFF8BBD0), Color(0xFFF44336)),
                CategoryExpense("Di chuyển", 22, "-850,000đ", "-8% so với tháng trước", Icons.Default.DirectionsCar, Color(0xFFBBDEFB), Color(0xFF4CAF50)),
                CategoryExpense("Sức khỏe", 5, "-650,000đ", "Không đổi", Icons.Default.Favorite, Color(0xFFC8E6C9), Color.Gray)
            )

            items(categories) { category ->
                ExpenseCategoryCard(category)
            }

            item {
                FinancialOverviewCard()
            }

            item {
                StatisticsInfoBox()
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun HomeStatsHeader() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF212121),
        contentColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("2026", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Thg 5", style = MaterialTheme.typography.titleLarge)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }

            HeaderStat(label = "Chi tiêu", value = "1.271.533")
            HeaderStat(label = "Thu nhập", value = "24.313")
            HeaderStat(label = "Số dư", value = "-1.247.220")
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
fun FinancialOverviewCard() {
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

            OverviewRow(Icons.Default.ArrowUpward, "Số dư đầu tháng", "+15,500,000đ", Color(0xFF4CAF50))
            Spacer(modifier = Modifier.height(12.dp))
            OverviewRow(Icons.Default.ArrowDownward, "Tổng chi tiêu", "-8,950,000đ", Color(0xFFF44336))
            
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
                    Text("6,550,000đ", fontWeight = FontWeight.Bold, color = Color(0xFF1976D2), fontSize = 18.sp)
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
