package com.example.billbuddy.ui.screens.debt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.billbuddy.data.model.Debt
import com.example.billbuddy.data.model.DebtStatus
import com.example.billbuddy.ui.viewmodel.DebtViewModel
import com.example.billbuddy.utils.Resource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailScreen(
    debtId: String,
    viewModel: DebtViewModel,
    onNavigateBack: () -> Unit
) {
    val debtState by viewModel.debtDetailState
    val currentUserId = viewModel.currentUserId ?: ""

    LaunchedEffect(debtId) {
        viewModel.loadDebtById(debtId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Chi tiết khoản nợ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        when (debtState) {
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Resource.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = (debtState as Resource.Error).message ?: "Lỗi tải dữ liệu", color = Color.Red)
                }
            }
            is Resource.Success -> {
                val debt = (debtState as Resource.Success).data ?: return@Scaffold
                
                // Load partner email
                LaunchedEffect(debt.documentId) {
                    viewModel.loadPartnerEmail(debt, currentUserId)
                }
                val partnerDisplay = viewModel.partnerEmails[debt.documentId] ?: "Đang tải..."

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    DebtMainInfoCard(debt, currentUserId, partnerDisplay)
                    DebtDetailedInfoCard(debt)
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    ActionButtons(debt, currentUserId, viewModel, onNavigateBack)
                }
            }
        }
    }
}

@Composable
fun DebtMainInfoCard(debt: Debt, currentUserId: String, partnerDisplay: String) {
    val isCreditor = debt.creditorId == currentUserId
    val numberFormat = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = if (isCreditor) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCreditor) Icons.Default.CallMade else Icons.Default.CallReceived,
                        contentDescription = null,
                        tint = if (isCreditor) Color(0xFF388E3C) else Color(0xFFD32F2F),
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Text(
                text = if (isCreditor) "Bạn đã cho vay" else "Bạn đang nợ",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            
            Text(
                text = numberFormat.format(debt.amount),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCreditor) Color(0xFF388E3C) else Color(0xFFD32F2F)
                )
            )
            
            Text(
                text = partnerDisplay,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (debt.status == DebtStatus.PENDING) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
            ) {
                Text(
                    text = if (debt.status == DebtStatus.PENDING) "CHƯA THANH TOÁN" else "ĐÃ THANH TOÁN",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (debt.status == DebtStatus.PENDING) Color(0xFFE65100) else Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
fun DebtDetailedInfoCard(debt: Debt) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dueDateStr = debt.dueDate?.toDate()?.let { dateFormat.format(it) } ?: "Chưa có hạn"
    val createdAtStr = debt.createdAt?.toDate()?.let { dateFormat.format(it) } ?: "N/A"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoRow(label = "Nội dung", value = debt.description)
            if (debt.note.isNotEmpty()) {
                InfoRow(label = "Ghi chú", value = debt.note)
            }
            InfoRow(label = "Ngày tạo", value = createdAtStr)
            InfoRow(label = "Ngày đến hạn", value = dueDateStr)
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ActionButtons(
    debt: Debt,
    currentUserId: String,
    viewModel: DebtViewModel,
    onNavigateBack: () -> Unit
) {
    val isCreditor = debt.creditorId == currentUserId
    val isDebtor = debt.debtorId == currentUserId
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (debt.status == DebtStatus.PENDING) {
            if (isDebtor) {
                // Debtor sees "Pay" (Ideally this should navigate to AddExpenseScreen)
                Button(
                    onClick = { 
                        viewModel.settleDebt(debt)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E49E2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("THANH TOÁN (GHI CHI TIÊU)", fontWeight = FontWeight.Bold)
                }
            } else {
                // Creditor sees wait status
                OutlinedButton(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CHỜ THANH TOÁN", fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // If SETTLED
            if (isCreditor) {
                // ONLY Creditor can delete settled debt history
                OutlinedButton(
                    onClick = { 
                        viewModel.deleteDebt(debt.documentId)
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("XÓA LỊCH SỬ NỢ", fontWeight = FontWeight.Bold)
                }
                
                // Creditor can undo settlement if it was accidental
                TextButton(
                    onClick = { 
                        viewModel.updateDebtStatus(debt.documentId, DebtStatus.PENDING)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Đánh dấu là chưa trả", color = Color.Gray)
                }
            } else {
                // Debtor sees completed status
                Button(
                    onClick = { },
                    enabled = false,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("ĐÃ THANH TOÁN", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
