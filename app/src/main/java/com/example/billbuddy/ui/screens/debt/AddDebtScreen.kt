package com.example.billbuddy.ui.screens.debt

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(
    onNavigateBack: () -> Unit,
    onSaveDebt: (description: String, amount: String, dueDate: String, isCreditor: Boolean) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var debtorName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2025-06-15") }
    var isCreditor by remember { mutableStateOf(true) } // true = cho vay, false = đi vay

    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            dueDate = "$year-${(month + 1).toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm khoản nợ", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF8F9FA))
                .verticalScroll(rememberScrollState())
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF5E49E2),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Thông tin khoản nợ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    // Loại khoản nợ
                    Column {
                        Text("Loại khoản nợ", color = Color.Gray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable { isCreditor = true },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCreditor) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                                border = if (isCreditor)
                                    androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF4CAF50))
                                else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CallMade,
                                        contentDescription = null,
                                        tint = if (isCreditor) Color(0xFF388E3C) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Cho vay",
                                        color = if (isCreditor) Color(0xFF388E3C) else Color.Gray,
                                        fontWeight = if (isCreditor) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable { isCreditor = false },
                                shape = RoundedCornerShape(8.dp),
                                color = if (!isCreditor) Color(0xFFFFEBEE) else Color(0xFFF5F5F5),
                                border = if (!isCreditor)
                                    androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF44336))
                                else null
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CallReceived,
                                        contentDescription = null,
                                        tint = if (!isCreditor) Color(0xFFD32F2F) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Đi vay",
                                        color = if (!isCreditor) Color(0xFFD32F2F) else Color.Gray,
                                        fontWeight = if (!isCreditor) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Tên người
                    Column {
                        Text(
                            if (isCreditor) "Người vay" else "Người cho vay",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        OutlinedTextField(
                            value = debtorName,
                            onValueChange = { debtorName = it },
                            placeholder = { Text("Nhập tên") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    // Số tiền
                    Column {
                        Text("Số tiền", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it },
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    // Mô tả
                    Column {
                        Text("Mô tả", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Lý do vay/cho vay...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    // Ngày đến hạn
                    Column {
                        Text("Ngày đến hạn", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = dueDate,
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

                    // Ghi chú
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

                    // Buttons
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
                            onClick = { onSaveDebt(description, amount, dueDate, isCreditor) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E49E2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Lưu", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
