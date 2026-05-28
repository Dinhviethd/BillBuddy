package com.example.billbuddy.ui.screens.expense

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.data.model.AppNotification
import com.example.billbuddy.data.model.NotificationType
import com.example.billbuddy.ui.components.NotificationIconButton
import com.example.billbuddy.ui.viewmodel.ExpenseViewModel
import com.example.billbuddy.utils.Resource
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val initialDate = remember {
        val calendar = Calendar.getInstance()
        String.format(
            Locale.US,
            "%04d-%02d-%02d",
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    var date by remember { mutableStateOf(initialDate) }
    var selectedCategoryId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedDebtId by remember { mutableStateOf<String?>(null) }

    val expenseState by viewModel.expenseState.collectAsState()
    val categories = expenseState.categories
    val pendingDebts = expenseState.pendingDebts
    val notifications = expenseState.notifications

    val saveState by viewModel.saveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(saveState) {
        when (val result = saveState) {
            is Resource.Success -> {
                viewModel.clearSaveState()
                onNavigateBack()
            }
            is Resource.Error -> {
                snackbarHostState.showSnackbar(result.message ?: "Lưu thất bại")
                viewModel.clearSaveState()
            }
            else -> Unit
        }
    }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            date = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, dayOfMonth)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm mới chi tiêu", fontWeight = FontWeight.Bold) },
                actions = {
                    NotificationIconButton(
                        notifications = notifications,
                        onRemoveNotification = { viewModel.removeNotification(it) },
                        onClearAll = { viewModel.clearAllNotifications() }
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (notifications.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                notifications.forEach { notify ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    ) {
                                        Icon(
                                            imageVector = when(notify.type) {
                                                NotificationType.URGENT -> Icons.Default.Error
                                                else -> Icons.Default.Notifications
                                            },
                                            contentDescription = null,
                                            tint = if (notify.type == NotificationType.URGENT) Color.Red else Color(0xFFE65100),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            notify.message, 
                                            color = if (notify.type == NotificationType.URGENT) Color.Red else Color(0xFFE65100), 
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = Color(0xFF5E49E2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Thông tin mới",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }


                    Column {
                        Text("Ngày", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = date,
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() },
                            enabled = false,
                            trailingIcon = {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { datePickerDialog.show() }
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.LightGray,
                                disabledTrailingIconColor = Color.Black
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }


                    Column {
                        Text("Danh mục chi tiêu", color = Color.Gray, fontSize = 14.sp)
                        val selectedCategoryName = categories.find { it.documentId == selectedCategoryId }?.name ?: "Chưa chọn"
                        Text(
                            text = selectedCategoryName,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.chunked(3).forEach { rowCategories ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowCategories.forEach { category ->
                                        FilterChip(
                                            selected = selectedCategoryId == category.documentId,
                                            onClick = { selectedCategoryId = category.documentId },
                                            label = { Text(category.name) }
                                        )
                                    }
                                }
                            }
                        }
                    }


                    Column {
                        Text("Số tiền chi tiêu", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }


                    Column {
                        Text("Ghi chú (tùy chọn)", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = { Text("Thêm ghi chú...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    if (pendingDebts.isNotEmpty()) {
                        Column {
                            Text("Thanh toán nợ (tùy chọn)", color = Color.Gray, fontSize = 14.sp)
                            var expanded by remember { mutableStateOf(false) }
                            val selectedDebt = pendingDebts.find { it.documentId == selectedDebtId }
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { expanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                                ) {
                                    Text(selectedDebt?.description ?: "Chọn khoản nợ để trả")
                                }
                                
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Không thanh toán nợ") },
                                        onClick = {
                                            selectedDebtId = null
                                            expanded = false
                                        }
                                    )
                                    pendingDebts.forEach { debt ->
                                        DropdownMenuItem(
                                            text = { Text("${debt.description} (${debt.amount} đ)") },
                                            onClick = {
                                                selectedDebtId = debt.documentId
                                                if (amount.isBlank()) amount = debt.amount.toString()
                                                if (note.isBlank()) note = "Trả nợ: ${debt.description}"
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Hủy", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                                if (selectedCategoryId.isBlank() || parsedAmount <= 0.0) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Vui lòng nhập danh mục và số tiền hợp lệ")
                                    }
                                } else {
                                    viewModel.addExpense(date, selectedCategoryId, parsedAmount, note.trim(), selectedDebtId)
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E49E2)),
                            shape = RoundedCornerShape(8.dp),
                            enabled = saveState !is Resource.Loading
                        ) {
                            Text("Lưu", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
