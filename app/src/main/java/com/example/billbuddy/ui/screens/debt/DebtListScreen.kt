package com.example.billbuddy.ui.screens.debt

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
import com.example.billbuddy.data.model.AppNotification
import com.example.billbuddy.data.model.Debt
import com.example.billbuddy.data.model.DebtStatus
import com.example.billbuddy.ui.theme.AppBackground
import com.example.billbuddy.ui.components.NotificationIconButton
import com.example.billbuddy.ui.viewmodel.DebtViewModel
import com.example.billbuddy.utils.Resource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtListScreen(
    viewModel: DebtViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToAddDebt: () -> Unit,
    onNavigateToDebtDetail: (String) -> Unit,
    notifications: List<AppNotification> = emptyList()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val debtsState by viewModel.debtsState
    val currentUserId = viewModel.currentUserId ?: ""

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Khoản nợ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    NotificationIconButton(
                        notifications = notifications
                    )
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
        when (val resource = debtsState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = resource.message ?: "Unknown Error", color = Color.Red)
                }
            }
            is Resource.Success -> {
                val debts = resource.data ?: emptyList()

                val filteredDebts = when (selectedTab) {
                    0 -> debts
                    1 -> debts.filter { it.status == DebtStatus.PENDING }
                    2 -> debts.filter { it.status == DebtStatus.SETTLED }
                    else -> debts
                }

                val totalLent     = debts.filter { it.creditorId == currentUserId }.sumOf { it.amount }
                val totalBorrowed = debts.filter { it.debtorId   == currentUserId }.sumOf { it.amount }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        DebtSummaryCard(totalLent, totalBorrowed)
                    }

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

                    items(filteredDebts) { debt ->
                        LaunchedEffect(debt.documentId) {
                            viewModel.loadPartnerEmail(debt, currentUserId)
                        }
                        val partnerEmail = viewModel.partnerEmails[debt.documentId]
                        DebtCard(
                            debt = debt,
                            currentUserId = currentUserId,
                            partnerDisplay = partnerEmail ?: "Đang tải...",
                            onClick = { onNavigateToDebtDetail(debt.documentId) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun DebtSummaryCard(totalLent: Long, totalBorrowed: Long) {
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    val balance = totalLent - totalBorrowed

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        color = Color(0xFF212121),
        contentColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        numberFormat.format(totalLent),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF4CAF50)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Đi vay", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        numberFormat.format(totalBorrowed),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFFF44336)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Số dư", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(
                        numberFormat.format(balance),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (balance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
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
fun DebtCard(debt: Debt, currentUserId: String, partnerDisplay: String, onClick: () -> Unit) {
    val isCreditor = debt.creditorId == currentUserId

    // Bỏ phần tính partnerDisplay cũ, dùng param truyền vào
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val dateStr = debt.dueDate?.toDate()?.let { dateFormat.format(it) } ?: "Chưa có hạn"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(10.dp),
                color = if (isCreditor) Color(0xFFC8E6C9).copy(alpha = 0.6f)
                else Color(0xFFFFCDD2).copy(alpha = 0.6f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCreditor) Icons.Default.CallMade
                        else Icons.Default.CallReceived,
                        contentDescription = null,
                        tint = if (isCreditor) Color(0xFF388E3C) else Color(0xFFD32F2F),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = partnerDisplay,  // ← hiển thị email hoặc "Đang tải..."
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
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isCreditor) "+${numberFormat.format(debt.amount)}"
                    else "-${numberFormat.format(debt.amount)}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isCreditor) Color(0xFF388E3C) else Color(0xFFD32F2F)
                )
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (debt.status == DebtStatus.PENDING) Color(0xFFFFF3E0)
                    else Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = if (debt.status == DebtStatus.PENDING) "Chưa trả" else "Đã trả",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (debt.status == DebtStatus.PENDING) Color(0xFFE65100)
                        else Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}