package com.example.billbuddy.ui.screens.debt

import android.app.DatePickerDialog
import android.widget.Toast
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.billbuddy.ui.viewmodel.DebtViewModel
import com.example.billbuddy.utils.Resource
import com.google.firebase.Timestamp
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(
    viewModel: DebtViewModel,
    onNavigateBack: () -> Unit,
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var debtorEmail by remember { mutableStateOf("") }  // ← nhập email thay vì tên
    var note by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf<Timestamp?>(null) }
    var dueDateDisplay by remember { mutableStateOf("Chọn ngày") }

    val addDebtState by viewModel.addDebtState
    val context = LocalContext.current

    // Xử lý kết quả sau khi lưu
    LaunchedEffect(addDebtState) {
        when (val state = addDebtState) {
            is Resource.Success -> {
                Toast.makeText(context, "Đã thêm khoản nợ!", Toast.LENGTH_SHORT).show()
                viewModel.resetAddDebtState()
                onNavigateBack()
            }
            is Resource.Error -> {
                Toast.makeText(context, state.message ?: "Lỗi", Toast.LENGTH_LONG).show()
                viewModel.resetAddDebtState()
            }
            else -> Unit
        }
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val cal = Calendar.getInstance().apply { set(year, month, day) }
            dueDate = Timestamp(cal.time)
            dueDateDisplay = "$day/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Thêm khoản cho vay", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    // Email người vay
                    Column {
                        Text("Email người vay", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = debtorEmail,
                            onValueChange = { debtorEmail = it },
                            placeholder = { Text("example@email.com") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.LightGray,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )
                    }

                    // Số tiền
                    Column {
                        Text("Số tiền (VNĐ)", color = Color.Gray, fontSize = 14.sp)
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { amount = it.filter { c -> c.isDigit() } },
                            placeholder = { Text("0") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next
                            ),
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
                            placeholder = { Text("Lý do cho vay...") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
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
                            value = dueDateDisplay,
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
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Hủy", color = Color.Gray)
                        }
                        Button(
                            onClick = {
                                val amountLong = amount.toLongOrNull() ?: 0L
                                if (debtorEmail.isBlank()) {
                                    Toast.makeText(context, "Vui lòng nhập email người vay", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (amountLong <= 0L) {
                                    Toast.makeText(context, "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                viewModel.addDebtByEmail(
                                    debtorEmail = debtorEmail.trim(),
                                    amount = amountLong,
                                    description = description,
                                    note = note,
                                    dueDate = dueDate
                                )
                            },
                            enabled = addDebtState !is Resource.Loading,
                            modifier = Modifier.weight(1f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E49E2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (addDebtState is Resource.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Lưu", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}