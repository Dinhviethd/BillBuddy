package com.example.billbuddy.ui.screens.debt

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.ui.theme.AppBackground

data class DebtItem(
    val name: String,
    val description: String,
    val amount: String,
    val dueDate: String,
    val isCreditor: Boolean, // true = cho vay, false = đi vay
    val status: String // "PENDING" or "SETTLED"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddDebt: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val debts = listOf(
        DebtItem("Nguyễn Văn A", "Cho mượn tiền mặt", "+200,000đ", "15/06/2025", true, "PENDING"),
        DebtItem("Trần Thị B", "Trả tiền ăn trưa", "-150,000đ", "20/05/2025", false, "PENDING"),
        DebtItem("Lê Văn C", "Cho mượn mua sách", "+80,000đ", "01/05/2025", true, "SETTLED"),
        DebtItem("Phạm Thị D", "Góp tiền quà sinh nhật", "-120,000đ", "10/04/2025", false, "SETTLED")
    )

    val filteredDebts = when (selectedTab) {
        0 -> debts
        1 -> debts.filter { it.status == "PENDING" }
        2 -> debts.filter { it.status == "SETTLED" }
        else -> debts
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Khoản nợ",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddDebt,
                shape = CircleShape,
                containerColor = Color(0xFFD47500),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(28.dp))
            }
        },
        containerColor = AppBackground
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Summary Card
            item {
                DebtSummaryCard()
            }

            // Tab Filters
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DebtTabButton(
                        text = "Tất cả",
                        isSelected = selectedTab == 0,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 0 }
                    )
                    DebtTabButton(
                        text = "Chưa trả",
                        isSelected = selectedTab == 1,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 1 }
                    )
                    DebtTabButton(
                        text = "Đã trả",
                        isSelected = selectedTab == 2,
                        modifier = Modifier.weight(1f),
                        onClick = { selectedTab = 2 }
                    )
                }
            }

            // Section Title
            item {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        text = "Danh sách khoản nợ",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${filteredDebts.size} khoản",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Debt Items
            items(filteredDebts) { debt ->
                DebtCard(debt)
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun DebtSummaryCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF212121),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Tổng quan khoản nợ",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Cho vay", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        "+280,000đ",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Đi vay", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        "-270,000đ",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF44336)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Số dư", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        "+10,000đ",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
            }
        }
    }
}

@Composable
fun DebtTabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
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
fun DebtCard(debt: DebtItem) {
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
            // Icon
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (debt.isCreditor) Color(0xFFC8E6C9).copy(alpha = 0.6f)
                else Color(0xFFFFCDD2).copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (debt.isCreditor) Icons.Default.CallMade
                        else Icons.Default.CallReceived,
                        contentDescription = null,
                        tint = if (debt.isCreditor) Color(0xFF388E3C) else Color(0xFFD32F2F),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = debt.name,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = debt.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = debt.dueDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            // Amount & Status
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = debt.amount,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (debt.isCreditor) Color(0xFF388E3C) else Color(0xFFD32F2F)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (debt.status == "PENDING") Color(0xFFFFF3E0)
                    else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = if (debt.status == "PENDING") "Chưa trả" else "Đã trả",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (debt.status == "PENDING") Color(0xFFE65100)
                        else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}
